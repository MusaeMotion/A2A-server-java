package com.musaemotion.a2a.agent.server.mcp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.agent.server.agent.AgentGeneralResponse;
import com.musaemotion.a2a.agent.server.agent.AgentRequest;
import com.musaemotion.a2a.agent.server.agent.AgentResponseStatus;
import com.musaemotion.a2a.agent.server.agent.AgentService;
import com.musaemotion.a2a.agent.server.mcp.manager.ToolManager;
import com.musaemotion.a2a.agent.server.mcp.tool.AbstractMcpToToolService;
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.UsageTokens;
import com.musaemotion.a2a.common.constant.MediaType;
import com.musaemotion.a2a.common.request.SendMessageRequest;
import com.musaemotion.agent.AgentPromptProvider;
import com.musaemotion.agent.BasisAgent;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.musaemotion.a2a.common.constant.MetaDataKey.CONVERSATION_ID;
import static com.musaemotion.a2a.common.constant.MetaDataKey.INPUT_MESSAGE_METADATA;
import static com.musaemotion.agent.BasisAgent.STATE;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/21  15:44
 * @description mcp智能体
 */
@Slf4j
public class McpHostAgent implements AgentService {

	/**
	 * 智能体
	 */
	private BasisAgent basisAgent;

	/**
	 * 可观察性
	 */
	private ObservationRegistry observationRegistry;


	/**
	 * 聊天记录
	 */
	private ChatMemoryRepository chatMemoryRepository;

	/**
	 * 聊天模型对象
	 */
	private ChatModel chatModel;

	/**
	 * 动态工具服务
	 */
	private ToolManager toolManager;

	/**
	 * 提示词提供者
	 */
	private AgentPromptProvider agentPromptProvider;

	/**
	 * a2a Server 配置
	 */
	private A2aServerProperties serverProperties;

	// 资源和提示词 转工具
	private List<AbstractMcpToToolService> mcpToToolServices;
	/**
	 * 构造函数
	 * @param chatModel
	 * @param observationRegistry
	 * @param chatMemoryRepository
	 * @param toolManager
	 */
	public McpHostAgent(ChatModel chatModel, ObservationRegistry observationRegistry, ChatMemoryRepository chatMemoryRepository, ToolManager toolManager, AgentPromptProvider agentPromptProvider, A2aServerProperties serverProperties, List<AbstractMcpToToolService> mcpToToolServices) {
		this.chatModel = chatModel;
		this.observationRegistry = observationRegistry;
		this.chatMemoryRepository = chatMemoryRepository;
		this.toolManager = toolManager;
		this.agentPromptProvider = agentPromptProvider;
		this.serverProperties = serverProperties;
		this.mcpToToolServices = mcpToToolServices;
		this.createHostAgent();
	}

	/**
	 * 创建host智能体
	 */
	private void createHostAgent() {
		var toolCallbacks = Lists.newArrayList(ToolCallbacks.from(this));
		/*
		暂时不使用资源获取和提示词工具，LLM无法理解
		if (this.mcpToToolServices != null) {
			List<ToolCallback> mcpToTools = this.mcpToToolServices.stream().map(item -> item.getToolCallback()).flatMap(Arrays::stream).collect(Collectors.toUnmodifiableList());
			toolCallbacks.addAll(mcpToTools);
		}*/
		this.basisAgent = BasisAgent.builder()
				.id("mcp-agent")
				.name("mcp-agent")
				// 读取两条记忆
				.chatMemorySize(10)
				.chatMemoryRepository(this.chatMemoryRepository)
				.chatClient(this.observationRegistry == null ? ChatClient.create(this.chatModel) : ChatClient.create(this.chatModel, this.observationRegistry))
				.observationRegistry(this.observationRegistry)
				.agentPromptProvider(this.agentPromptProvider)
				.toolCallbacks(toolCallbacks)
				.build();
	}

	/**
	 * 支持类型
	 * @return
	 */
	@Override
	public List<MediaType> supportedContentTypes() {
		return List.of(MediaType.TEXT, MediaType.APP_JSON, MediaType.IMAGE_PNG, MediaType.VIDEO_MP4, MediaType.TEXT_PLAIN);
	}

	/**
	 * agent名称
	 * @return
	 */
	@Override
	public String agentName() {
		return this.serverProperties.getName();
	}

	/**
	 * 使用模型
	 * @return
	 */
	@Override
	public String useModel() {
		return this.chatModel.getDefaultOptions().getModel();
	}


	/**
	 * 构建工具上下文
	 * @param input
	 * @return
	 */
	private Map<String, Object> buildToolContext(SendMessageRequest input) {
		Map<String, Object> state = new HashMap<>();
		state.put(INPUT_MESSAGE_METADATA, input.getMetadata());
		state.put(CONVERSATION_ID, input.getConversationId());
		Map<String, Object> toolContext = new HashMap<>();
		toolContext.put(STATE, state);
		return toolContext;
	}
	/**
	 * 流请求
	 * @param agentRequest
	 * @return
	 */
	@Override
	public Flux<AgentGeneralResponse> stream(AgentRequest agentRequest) {
		SendMessageRequest sendMessageRequest = agentRequest.toSendMessageRequest();
		Flux<ChatResponse>  flux = this.basisAgent.stream(
				sendMessageRequest,
				buildToolContext(sendMessageRequest),
				agentRequest.getParts().stream().filter(item -> item instanceof Common.FilePart).collect(Collectors.toUnmodifiableList())
		);
		return flux
				.filter(chatResponse ->{
					if(chatResponse.getResult()!=null) {
						return true;
					}
					return false;
				})
				.map(chatResponse -> {
					AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
					if ("STOP".equals(assistantMessage.getMetadata().get("finishReason")) ) {
						return AgentGeneralResponse.fromStreamChatResponse(chatResponse, AgentResponseStatus.COMPLETED);
					}
					return AgentGeneralResponse.fromStreamChatResponse(chatResponse, AgentResponseStatus.WORKING);
				})
				.doOnError(e -> log.error("Error occurred: {}", e.getMessage()));

	}

	/**
	 *
	 * @param agentRequest
	 * @return
	 */
	@Override
	public AgentGeneralResponse call(AgentRequest agentRequest) {
		SendMessageRequest sendMessageRequest = agentRequest.toSendMessageRequest();
		try {
			ChatResponse chatResponse = this.basisAgent.call(
					sendMessageRequest,
					buildToolContext(sendMessageRequest),
					agentRequest.getParts()
							.stream()
							.filter(item -> item instanceof Common.FilePart).collect(Collectors.toUnmodifiableList())
					);
			var usage = chatResponse.getMetadata().getUsage();
			return AgentGeneralResponse.fromText(chatResponse.getResult().getOutput().getText(), AgentResponseStatus.COMPLETED,
					UsageTokens.fromUsage(
							usage.getCompletionTokens(),
							usage.getPromptTokens(),
							usage.getTotalTokens()
					)
			);
		}
		catch (IllegalStateException illegalStateException){
			return AgentGeneralResponse.fromText("MCP工具返回错误："+illegalStateException.getMessage(), AgentResponseStatus.ERROR);
		}
		catch (Exception ex){
			return AgentGeneralResponse.fromText(ex.toString(), AgentResponseStatus.ERROR);
		}
	}

	/**
	 * 列出所有远程智能体
	 *
	 * @return
	 */
	@Tool(description = "列出所有mcp信息")
	public String discoverMcp() throws JsonProcessingException {
		String tools = this.toolManager.getMcpBasicsJson();
		log.warn("discoverMcp:{}", tools);
		return tools;
	}

	/**
	 * 构建模式
	 *
	 * @return
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private ChatModel chatModel;

		private ObservationRegistry observationRegistry;

		private ChatMemoryRepository chatMemoryRepository;

		private ToolManager toolManager;

		private AgentPromptProvider agentPromptProvider;

		private A2aServerProperties serverProperties;

		private List<AbstractMcpToToolService> mcpToToolServices;

		private Builder() {
		}

		public Builder chatModel(
				ChatModel chatModel) {
			this.chatModel = chatModel;
			return this;
		}

		public Builder observationRegistry(
				ObservationRegistry observationRegistry) {
			this.observationRegistry = observationRegistry;
			return this;
		}

		public Builder chatMemoryRepository(
				ChatMemoryRepository chatMemoryRepository) {
			this.chatMemoryRepository = chatMemoryRepository;
			return this;
		}

		public Builder toolManager(
				ToolManager toolManager) {
			this.toolManager = toolManager;
			return this;
		}

		public Builder agentPromptProvider(
				AgentPromptProvider agentPromptProvider) {
			this.agentPromptProvider = agentPromptProvider;
			return this;
		}

		public Builder a2aServerProperties(
				A2aServerProperties serverProperties) {
			this.serverProperties = serverProperties;
			return this;
		}
		public Builder mcpToToolServices(
				List<AbstractMcpToToolService> mcpToToolServices) {
			this.mcpToToolServices = mcpToToolServices;
			return this;
		}
		public McpHostAgent build() {
			Assert.notNull(this.chatModel, "chatModel 不能为空");
			Assert.notNull(this.chatMemoryRepository, "chatMemoryRepository 不能为空");
			Assert.notNull(this.agentPromptProvider, "agentPromptProvider 不能为空");
			McpHostAgent hostAgent = new McpHostAgent(
					this.chatModel,
					this.observationRegistry,
					this.chatMemoryRepository,
					this.toolManager,
					this.agentPromptProvider,
					this.serverProperties,
					this.mcpToToolServices
			);
			return hostAgent;
		}

	}
}
