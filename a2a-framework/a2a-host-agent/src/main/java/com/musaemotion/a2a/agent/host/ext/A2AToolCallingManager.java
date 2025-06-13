/*
 * Copyright (c) 2025 MusaeMotion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.musaemotion.a2a.agent.host.ext;

import com.musaemotion.agent.AgentPromptProvider;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.ai.tool.observation.DefaultToolCallingObservationConvention;
import org.springframework.ai.tool.observation.ToolCallingObservationContext;
import org.springframework.ai.tool.observation.ToolCallingObservationConvention;
import org.springframework.ai.tool.observation.ToolCallingObservationDocumentation;
import org.springframework.ai.tool.resolution.DelegatingToolCallbackResolver;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.musaemotion.agent.BasisAgent.STATE;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.ext
 * @project：a2a-github
 * @date：2025/5/21 13:14
 * @description：请完善描述
 */
@Slf4j
public class A2AToolCallingManager implements ToolCallingManager {

	public static String RETURN_DIRECT = "returnDirect";
	// @formatter:off

	private static final ObservationRegistry DEFAULT_OBSERVATION_REGISTRY
			= ObservationRegistry.NOOP;

	private static final ToolCallingObservationConvention DEFAULT_OBSERVATION_CONVENTION
			= new DefaultToolCallingObservationConvention();

	private static final ToolCallbackResolver DEFAULT_TOOL_CALLBACK_RESOLVER
			= new DelegatingToolCallbackResolver(List.of());

	private static final ToolExecutionExceptionProcessor DEFAULT_TOOL_EXECUTION_EXCEPTION_PROCESSOR
			= DefaultToolExecutionExceptionProcessor.builder().build();

	// @formatter:on

	private final ObservationRegistry observationRegistry;

	private final ToolCallbackResolver toolCallbackResolver;

	private final ToolExecutionExceptionProcessor toolExecutionExceptionProcessor;

	private ToolCallingObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	private final AgentPromptProvider agentPromptProvider;

	public A2AToolCallingManager(ObservationRegistry observationRegistry, ToolCallbackResolver toolCallbackResolver,
								 ToolExecutionExceptionProcessor toolExecutionExceptionProcessor, AgentPromptProvider agentPromptProvider) {
		Assert.notNull(observationRegistry, "observationRegistry cannot be null");
		Assert.notNull(toolCallbackResolver, "toolCallbackResolver cannot be null");
		Assert.notNull(toolExecutionExceptionProcessor, "toolCallExceptionConverter cannot be null");

		this.observationRegistry = observationRegistry;
		this.toolCallbackResolver = toolCallbackResolver;
		this.toolExecutionExceptionProcessor = toolExecutionExceptionProcessor;
		this.agentPromptProvider = agentPromptProvider;
	}

	@Override
	public List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions chatOptions) {
		Assert.notNull(chatOptions, "chatOptions cannot be null");

		List<ToolCallback> toolCallbacks = new ArrayList<>(chatOptions.getToolCallbacks());
		for (String toolName : chatOptions.getToolNames()) {
			// Skip the tool if it is already present in the request toolCallbacks.
			// That might happen if a tool is defined in the options
			// both as a ToolCallback and as a tool name.
			if (chatOptions.getToolCallbacks()
					.stream()
					.anyMatch(tool -> tool.getToolDefinition().name().equals(toolName))) {
				continue;
			}
			ToolCallback toolCallback = this.toolCallbackResolver.resolve(toolName);
			if (toolCallback == null) {
				throw new IllegalStateException("No ToolCallback found for tool name: " + toolName);
			}
			toolCallbacks.add(toolCallback);
		}

		return toolCallbacks.stream().map(ToolCallback::getToolDefinition).toList();
	}

	@Override
	public ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse) {
		Assert.notNull(prompt, "prompt cannot be null");
		Assert.notNull(chatResponse, "chatResponse cannot be null");

		Optional<Generation> toolCallGeneration = chatResponse.getResults()
				.stream()
				.filter(g -> !CollectionUtils.isEmpty(g.getOutput().getToolCalls()))
				.findFirst();

		if (toolCallGeneration.isEmpty()) {
			throw new IllegalStateException("No tool call requested by the chat model");
		}

		AssistantMessage assistantMessage = toolCallGeneration.get().getOutput();

		ToolContext toolContext = buildToolContext(prompt, assistantMessage);

		A2AToolCallingManager.InternalToolExecutionResult internalToolExecutionResult = executeToolCall(prompt, assistantMessage,
				toolContext);

		List<Message> conversationHistory = buildConversationHistoryAfterToolExecution(prompt.getInstructions(),
				assistantMessage, internalToolExecutionResult.toolResponseMessage(), (Map<String, Object>)toolContext.getContext().get(STATE));
		// 历史记录会有，工具调用响应AssistantMessage [messageType=ASSISTANT, toolCalls=[ToolCall[id=, type=function, name=listRemoteAgents, arguments={}]], textContent=, metadata={messageType=ASSISTANT}]
		// 还会增加一条 共享响应消息 消息类型是TOOL

		return ToolExecutionResult.builder()
				.conversationHistory(conversationHistory)
				.returnDirect(internalToolExecutionResult.returnDirect())
				.build();
	}

	private static ToolContext buildToolContext(Prompt prompt, AssistantMessage assistantMessage) {
		Map<String, Object> toolContextMap = Map.of();

		if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions
				&& !CollectionUtils.isEmpty(toolCallingChatOptions.getToolContext())) {
			toolContextMap = new HashMap<>(toolCallingChatOptions.getToolContext());

			List<Message> messageHistory = new ArrayList<>(prompt.copy().getInstructions());
			messageHistory.add(new AssistantMessage(assistantMessage.getText(), assistantMessage.getMetadata(),
					assistantMessage.getToolCalls()));

			toolContextMap.put(ToolContext.TOOL_CALL_HISTORY,
					buildConversationHistoryBeforeToolExecution(prompt, assistantMessage));
		}

		return new ToolContext(toolContextMap);
	}

	private static List<Message> buildConversationHistoryBeforeToolExecution(Prompt prompt,
																			 AssistantMessage assistantMessage) {
		List<Message> messageHistory = new ArrayList<>(prompt.copy().getInstructions());
		messageHistory.add(new AssistantMessage(assistantMessage.getText(), assistantMessage.getMetadata(),
				assistantMessage.getToolCalls()));
		return messageHistory;
	}

	/**
	 * Execute the tool call and return the response message.
	 */
	private A2AToolCallingManager.InternalToolExecutionResult executeToolCall(Prompt prompt, AssistantMessage assistantMessage,
																			  ToolContext toolContext) {
		List<ToolCallback> toolCallbacks = List.of();
		if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
			toolCallbacks = toolCallingChatOptions.getToolCallbacks();
		}

		List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();

		AtomicReference<Boolean> returnDirect = new AtomicReference<>();

		for (AssistantMessage.ToolCall toolCall : assistantMessage.getToolCalls()) {

			log.debug("Executing tool call: {}", toolCall.name());

			String toolName = toolCall.name();
			String toolInputArguments = toolCall.arguments();

			ToolCallback toolCallback = toolCallbacks.stream()
					.filter(tool -> toolName.equals(tool.getToolDefinition().name()))
					.findFirst()
					.orElseGet(() -> this.toolCallbackResolver.resolve(toolName));

			if (toolCallback == null) {
				throw new IllegalStateException("No ToolCallback found for tool name: " + toolName);
			}

			if (returnDirect.get() == null) {
				returnDirect.set(toolCallback.getToolMetadata().returnDirect());
			}
			else {
				returnDirect.set(returnDirect.get() && toolCallback.getToolMetadata().returnDirect());
			}

			ToolCallingObservationContext observationContext = ToolCallingObservationContext.builder()
					.toolDefinition(toolCallback.getToolDefinition())
					.toolMetadata(toolCallback.getToolMetadata())
					.toolCallArguments(toolInputArguments)
					.build();

			String toolCallResult = ToolCallingObservationDocumentation.TOOL_CALL
					.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
							this.observationRegistry)
					.observe(() -> {
						String toolResult;
						try {
							toolResult = toolCallback.call(toolInputArguments, toolContext);
							Object state = toolContext.getContext().get(STATE);
							if(state !=null && ((Map<String, Object>)state).containsKey(RETURN_DIRECT)
							) {
								var stateMap = (Map<String, Object>)state;
								returnDirect.set((Boolean) stateMap.get(RETURN_DIRECT));
							}
						}
						catch (ToolExecutionException ex) {
							toolResult = this.toolExecutionExceptionProcessor.process(ex);
						}
						observationContext.setToolCallResult(toolResult);
						return toolResult;
					});

			toolResponses.add(new ToolResponseMessage.ToolResponse(toolCall.id(), toolName,
					toolCallResult != null ? toolCallResult : ""));
		}

		return new A2AToolCallingManager.InternalToolExecutionResult(new ToolResponseMessage(toolResponses, Map.of()), returnDirect.get());
	}
	/**
	 * 当前对话执行之后，构建历史聊天记录
	 * @param previousMessages
	 * @param assistantMessage
	 * @param toolResponseMessage
	 * @return
	 */
	private List<Message> buildConversationHistoryAfterToolExecution(List<Message> previousMessages,
																	 AssistantMessage assistantMessage, ToolResponseMessage toolResponseMessage,
																	 Map<String, Object> state) {
		List<Message> messages = new ArrayList<>(previousMessages);
		// 工具触发调用
		messages.add(assistantMessage);
		// 工具触发调用响应
		messages.add(toolResponseMessage);

		/*
		if(!state.containsKey(RETURN_DIRECT)) {
			Message newSystem = new SystemMessage(this.hostAgentPromptService.hostAgentSystemPrompt(state));
			messages.add(newSystem);
			return  messages;
		} */
		return messages;
	}
	private List<Message> buildConversationHistoryAfterToolExecution(List<Message> previousMessages,
																	 AssistantMessage assistantMessage, ToolResponseMessage toolResponseMessage) {
		List<Message> messages = new ArrayList<>(previousMessages);
		messages.add(assistantMessage);
		messages.add(toolResponseMessage);
		return messages;
	}

	public void setObservationConvention(ToolCallingObservationConvention observationConvention) {
		this.observationConvention = observationConvention;
	}

	public static A2AToolCallingManager.Builder builder() {
		return new A2AToolCallingManager.Builder();
	}

	private record InternalToolExecutionResult(ToolResponseMessage toolResponseMessage, boolean returnDirect) {
	}

	public final static class Builder {

		private ObservationRegistry observationRegistry = DEFAULT_OBSERVATION_REGISTRY;

		private ToolCallbackResolver toolCallbackResolver = DEFAULT_TOOL_CALLBACK_RESOLVER;

		private ToolExecutionExceptionProcessor toolExecutionExceptionProcessor = DEFAULT_TOOL_EXECUTION_EXCEPTION_PROCESSOR;


		private AgentPromptProvider agentPromptProvider;

		private Builder() {
		}

		public A2AToolCallingManager.Builder observationRegistry(ObservationRegistry observationRegistry) {
			this.observationRegistry = observationRegistry;
			return this;
		}

		public A2AToolCallingManager.Builder toolCallbackResolver(ToolCallbackResolver toolCallbackResolver) {
			this.toolCallbackResolver = toolCallbackResolver;
			return this;
		}

		public A2AToolCallingManager.Builder hostAgentPromptService(
				AgentPromptProvider agentPromptProvider) {
			this.agentPromptProvider = agentPromptProvider;
			return this;
		}
		public A2AToolCallingManager.Builder toolExecutionExceptionProcessor(
				ToolExecutionExceptionProcessor toolExecutionExceptionProcessor) {
			this.toolExecutionExceptionProcessor = toolExecutionExceptionProcessor;
			return this;
		}

		public A2AToolCallingManager build() {
			return new A2AToolCallingManager(this.observationRegistry, this.toolCallbackResolver,
					this.toolExecutionExceptionProcessor, agentPromptProvider);
		}

	}

}
