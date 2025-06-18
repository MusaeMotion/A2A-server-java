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

package com.musaemotion.a2a.agent.host.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.musaemotion.a2a.agent.client.server.PushNotificationServer;
import com.musaemotion.a2a.agent.host.core.DefaultSendTaskCallbackHandle;
import com.musaemotion.a2a.agent.host.core.HostAgent;
import com.musaemotion.a2a.agent.host.model.response.CommonMessageExt;
import com.musaemotion.a2a.agent.host.model.response.SendMessageResponse;
import com.musaemotion.a2a.agent.host.properties.A2aHostAgentProperties;
import com.musaemotion.a2a.agent.host.provider.ChatModelProvider;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.common.constant.MessageRole;
import com.musaemotion.a2a.common.utils.GuidUtils;
import com.musaemotion.a2a.common.utils.JsonUtils;
import com.musaemotion.agent.AgentPromptProvider;
import com.musaemotion.agent.model.SendMessageRequest;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

import static com.musaemotion.a2a.common.constant.ArtifactDataKey.*;
import static com.musaemotion.a2a.common.constant.MetaDataKey.*;
import static com.musaemotion.agent.BasisAgent.STATE;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.service
 * @project：A2A
 * @date：2025/5/9 13:15
 * @description：请完善描述
 */
@Slf4j
@Service
public class ChatManager {

	/**
	 * 交谈管理器
	 */
	private AbstractConversationManager conversationManager;

	/**
	 * 消息管理器
	 */
	private AbstractMessageManager messageManager;

	/**
	 * 消息通知服务
	 */
	private PushNotificationServer pushNotificationServer;

	/**
	 * 智能体任务管理器
	 */
	private AbstractTaskCenterManager taskCenterManager;


	/**
	 * 远程智能体管理器
	 */
	private AbstractRemoteAgentManager remoteAgentManager;

	/**
	 * 可观察性
	 */
	private ObservationRegistry observationRegistry;

	/**
	 * host Agent 提示词service
	 */
	private AgentPromptProvider agentPromptProvider;

	/**
	 * hostAgent 配置
	 */
	private A2aHostAgentProperties a2aHostAgentProperties;

	/**
	 * 模型提供者
	 */
	private ChatModelProvider ChatModelProvider;

	/**
	 * 聊天记录
	 */
	private ChatMemoryRepository chatMemoryRepository;
	/**
	 * 构造hostAgent
	 *
	 * @return
	 */
	private HostAgent buildHostAgent() {
		var hostAgent = HostAgent.builder()
				.remoteAgentAddresses(
						CollectionUtils.isEmpty(a2aHostAgentProperties.getRemoteAgentAddresses()) ?
								Lists.newArrayList() : a2aHostAgentProperties.getRemoteAgentAddresses()
				)
				.pushNotificationServer(this.pushNotificationServer)
				.remoteAgentManager(this.remoteAgentManager)
				.taskCenterManager(this.taskCenterManager)
				.messageManager(this.messageManager)
				.observationRegistry(this.observationRegistry)
				.hostAgentPromptService(this.agentPromptProvider)
				.chatMemoryRepository(this.chatMemoryRepository)
				.chatModelProvider(this.ChatModelProvider)
				.sendTaskCallback(new DefaultSendTaskCallbackHandle(this.taskCenterManager))
				.build();
		return hostAgent;
	}

	/**
	 * @param ChatModelProvider
	 * @param a2aHostAgentProperties
	 * @param abstractConversationManager
	 * @param abstractMessageManager
	 * @param pushNotificationServer
	 */
	@Autowired
	public ChatManager(ChatModelProvider ChatModelProvider, A2aHostAgentProperties a2aHostAgentProperties, AbstractRemoteAgentManager remoteAgentManager, AbstractConversationManager abstractConversationManager, AbstractMessageManager abstractMessageManager, AbstractTaskCenterManager abstractTaskCenterManager, AgentPromptProvider agentPromptProvider, @Autowired(required = false) PushNotificationServer pushNotificationServer, @Autowired(required = false) ObservationRegistry observationRegistry, ChatMemoryRepository chatMemoryRepository) {
		this.conversationManager = abstractConversationManager;
		this.messageManager = abstractMessageManager;
		this.pushNotificationServer = pushNotificationServer;
		this.taskCenterManager = abstractTaskCenterManager;
		this.observationRegistry = observationRegistry;
		this.agentPromptProvider = agentPromptProvider;
		if (this.observationRegistry == null) {
			this.observationRegistry = ObservationRegistry.NOOP;
		}
		this.remoteAgentManager = remoteAgentManager;
		this.a2aHostAgentProperties = a2aHostAgentProperties;
		this.ChatModelProvider = ChatModelProvider;
		this.chatMemoryRepository = chatMemoryRepository;
	}

	/**
	 * 检查参数
	 *
	 * @param input
	 */
	private void checkSendMessageRequest(SendMessageRequest input) {
		if (!StringUtils.hasText(input.getContent())) {
			throw new IllegalArgumentException("content is null");
		}
		if (!StringUtils.hasText(input.getConversationId())) {
			throw new IllegalArgumentException("conversationId is null");
		}
		if (!this.conversationManager.exist(input.getConversationId())) {
			throw new IllegalArgumentException("conversation no exist for conversationId");
		}
	}


	/**
	 * 发送之前处理消息
	 *
	 * @param input
	 * @return
	 */
	private Common.Message sanitizeRequestMessage(SendMessageRequest input) {
		Boolean isEmpty = Optional.ofNullable(input.getMetadata()).map(m -> m.isEmpty()).orElse(true);
		Common.Message message = Common.Message.builder()
				.role(MessageRole.USER)
				.parts(input.getParams().getParts())
				.metadata(isEmpty ? Maps.newConcurrentMap() : input.getMetadata())
				.build();

		Optional<Common.Message> optionalMessage = this.messageManager.lastByConversationId(input.getConversationId());
		// 添加上一条消息
		if (optionalMessage.isPresent()) {
			message.getMetadata().put(LAST_MESSAGE_ID, optionalMessage.get().getMetadata().get(MESSAGE_ID));
		}
		return message;
	}

	/**
	 * 处理响应消息
	 *
	 * @param assistantMessage
	 * @param userMessage
	 * @return
	 */
	private Common.Message sanitizeResponseMessage(AssistantMessage assistantMessage, Common.Message userMessage, String messageId) {
		Common.Message message = null;
		// 这里也是响应内容，该逻辑是应对直接返回的消息，比如错误，或者输入内容的消息
		if (JsonUtils.isJsonString(assistantMessage.getText())) {
			message = Common.Message.builder()
					.role(MessageRole.AGENT)
					.metadata(assistantMessage.getMetadata())
					.parts(Lists.newArrayList())
					.build();
			// 表示输出结果，直接结束，表示完成STOP
			assistantMessage.getMetadata().put("finishReason", "STOP");
			try {
				JsonNode jsonNode = new ObjectMapper().readTree(assistantMessage.getText());


				Common.Message finalMessage = message;
				if(jsonNode instanceof ArrayNode){
					jsonNode.forEach(json -> {
						if (json.has(ARTIFACT_FILE_URL) || json.has(ARTIFACT_FILE_ID)) {
							Common.FilePart filePart = new Common.FilePart();
							filePart.setFile(Common.FileContent.builder()
									.uri(json.get(ARTIFACT_FILE_URL) == null ? "" : json.get(ARTIFACT_FILE_URL).textValue())
									.name(json.get(ARTIFACT_FILE_ID) == null ? "" : json.get(ARTIFACT_FILE_ID).textValue())
									.mimeType(json.get(ARTIFACT_MIME_TYPE) == null ? "" : json.get(ARTIFACT_MIME_TYPE).textValue())
									.build());
							finalMessage.getParts().add(filePart);
							return;
						}
						if (json.has("type") && json.get("type").textValue().equals("text")) {
							finalMessage.getParts().add(new ObjectMapper().convertValue(json, Common.TextPart.class));
							return;
						}
						Common.DataPart dataPart = new Common.DataPart();
						dataPart.setData(new ObjectMapper().convertValue(json, Map.class));
						finalMessage.getParts().add(dataPart);

					});
				}else{
					finalMessage.getParts().add(new Common.TextPart(jsonNode.toString()));
				}

			} catch (JsonProcessingException e) {
				throw new RuntimeException("响应内容反序列化失败");
			}
		} else {
			message = Common.Message.builder()
					.role(MessageRole.AGENT)
					.parts(Lists.newArrayList(new Common.TextPart(assistantMessage.getText())))
					.metadata(assistantMessage.getMetadata())
					.build();
		}
		message.getMetadata().put(LAST_MESSAGE_ID, userMessage.getMetadata().get(MESSAGE_ID).toString());
		message.getMetadata().put(CONVERSATION_ID, userMessage.getMetadata().get(CONVERSATION_ID).toString());
		message.getMetadata().put(MESSAGE_ID, StringUtils.hasText(messageId) ? messageId : GuidUtils.createGuid());
		return message;
	}

	/**
	 * 构建工具上下文信息，如果有的
	 * @param input
	 * @return
	 */
	private Map<String, Object> buildToolContext(SendMessageRequest input) {

		Map<String, Object> state = new HashMap<>();
		state.put(INPUT_MESSAGE_METADATA, input.getMetadata());
		state.put(CONVERSATION_ID, input.getConversationId());
		// 主任务Id, 用于后续异步任务主Id使用
		state.put(MAIN_TASK_ID, GuidUtils.createGuid());

		// 工具上下文
		Map<String, Object> toolContext = new HashMap<>();
		toolContext.put(STATE, state);

		return toolContext;
	}

	/**
	 * 之前处理
	 *
	 * @param input
	 * @return
	 */
	private Common.Message sendBefore(SendMessageRequest input) {
		this.checkSendMessageRequest(input);
		// 消息处理
		Common.Message userMessage = this.sanitizeRequestMessage(input);
		// 消息管理器保存
		this.messageManager.upsert(userMessage);
		return userMessage;
	}


	/**
	 * 后处理
	 *
	 * @param assistantMessage
	 * @param userMessage
	 * @return
	 */
	private Common.Message sendAfter(AssistantMessage assistantMessage, Common.Message userMessage) {
		return this.sendAfter(assistantMessage, userMessage, "");
	}

	/**
	 * 后处理
	 *
	 * @param assistantMessage
	 * @param userMessage
	 * @param messageId
	 * @return
	 */
	private Common.Message sendAfter(AssistantMessage assistantMessage, Common.Message userMessage, String messageId) {
		Common.Message agnetMessage = this.sanitizeResponseMessage(assistantMessage, userMessage, messageId);
		// 消息管理器保存
		this.messageManager.upsert(agnetMessage);
		return agnetMessage;
	}

	/**
	 * 加载task信息
	 *
	 * @param agnetMessage
	 * @return
	 */
	private CommonMessageExt loadTask(Common.Message agnetMessage) {
		var message = CommonMessageExt.fromMessage(agnetMessage);
		String lastMessageId = agnetMessage.getLastMessageId();
		if (StringUtils.hasText(lastMessageId)) {
			List<Task> tasks = this.taskCenterManager.listByInputMessageId(Lists.newArrayList(agnetMessage.getLastMessageId()));
			message.setTask(tasks);
		}
		return message;
	}

	/**
	 * 流请求包装消息
	 *
	 * @param messages
	 * @return
	 */
	private Common.Message streamFinishReasonMessage(List<Common.Message> messages) {
		var parts = messages.stream().map(item -> item.getParts()).flatMap(Collection::stream).collect(Collectors.toUnmodifiableList());
		String text = parts.stream().filter(part -> {
			return part instanceof Common.TextPart;
		}).map(item -> ((Common.TextPart) item).getText()).collect(Collectors.joining(""));
		var agentMessage = messages.get(messages.size() - 1);
		agentMessage.getParts().clear();
		agentMessage.getParts().add(new Common.TextPart(text));
		return agentMessage;
	}

	/**
	 * 同步请求
	 * @param input
	 * @return
	 */
	public SendMessageResponse<CommonMessageExt> call(SendMessageRequest input) {
		var hostAgent = this.buildHostAgent();
		Common.Message userMessage = this.sendBefore(input);
		AssistantMessage assistantMessage = hostAgent.call(input, this.buildToolContext(input), Lists.newArrayList());
		Common.Message agnetMessage = this.sendAfter(assistantMessage, userMessage);
		var message = loadTask(agnetMessage);
		// 删除删除通知sse
		SseEmitterManager.removeEmitter(input.getConversationId(), input.getMessageId());
		return SendMessageResponse.buildMessageResponse(
				message,
				input.getConversationId()
		);
	}


	/**
	 * 流请求
	 * @param input
	 * @return
	 */
	public Flux<SendMessageResponse> stream(SendMessageRequest input) {
		var hostAgent = this.buildHostAgent();
		Common.Message userMessage = this.sendBefore(input);
		Flux<AssistantMessage> fluxAssistantMessage = hostAgent.stream(input, this.buildToolContext(input), Lists.newArrayList());
		String messageId = GuidUtils.createGuid();
		List<Common.Message> messages = Lists.newArrayList();
		return fluxAssistantMessage.doFinally(i -> {
					// 删除删除通知sse
					SseEmitterManager.removeEmitter(input.getConversationId(), input.getMessageId());
					var agentMessage = this.streamFinishReasonMessage(messages);
					this.messageManager.upsert(agentMessage);
				})
				.map(assistantMessage -> {
					var agnetMessage = this.sanitizeResponseMessage(assistantMessage, userMessage, messageId);
					try {
						messages.add(agnetMessage);
						// 加载任务消息
						if ("STOP".equals(assistantMessage.getMetadata().get("finishReason")) || !assistantMessage.getMetadata().containsKey("finishReason")) {
							agnetMessage = loadTask(agnetMessage);
						}

					} catch (Exception e) {
						agnetMessage.setParts(Lists.newArrayList(new Common.TextPart("智能体出现异常")));
					}
					return SendMessageResponse.buildMessageResponse(
							agnetMessage,
							input.getConversationId());
				});
	}

}
