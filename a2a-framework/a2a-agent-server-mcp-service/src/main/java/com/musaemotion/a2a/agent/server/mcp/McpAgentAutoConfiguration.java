package com.musaemotion.a2a.agent.server.mcp;

import com.musaemotion.a2a.agent.server.mcp.tool.AbstractMcpToToolService;
import com.musaemotion.a2a.agent.server.notification.PushNotificationSenderService;
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import com.musaemotion.agent.AgentPromptProvider;
import com.musaemotion.a2a.agent.server.mcp.core.McpHostAgent;
import com.musaemotion.a2a.agent.server.mcp.manager.ToolManager;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/21  14:13
 * @description
 */
@Service
@Slf4j
@Configuration
@Component
@ComponentScan("com.musaemotion.a2a")
@RequiredArgsConstructor
public class McpAgentAutoConfiguration {

	/**
	 * 可观察性
	 */

	private ObservationRegistry observationRegistry;

	/**
	 * host Agent 提示词service
	 */
	private  AgentPromptProvider agentPromptProvider;

	/**
	 * 聊天记录
	 */
	private  ChatMemoryRepository chatMemoryRepository;

	/**
	 * 聊天模型
	 */
	private  ChatModel chatModel;

	/**
	 *  A2a 服务配置
	 */
	private  A2aServerProperties serverProperties;

	/**
	 * 配置
	 */
	private ToolManager toolManager;

	/**
	 * 通知服务
	 */
	private  PushNotificationSenderService pushNotificationSenderService;

	/**
	 *
	 */
	private List<AbstractMcpToToolService> mcpToToolServices;

	@Autowired
	public McpAgentAutoConfiguration(
			AgentPromptProvider agentPromptProvider,
			ChatMemoryRepository chatMemoryRepository,
			ChatModel chatModel,
			A2aServerProperties serverProperties,
			ToolManager toolManager,
			PushNotificationSenderService pushNotificationSenderService,
			@Autowired(required = false) ObservationRegistry observationRegistry,
			List<AbstractMcpToToolService> mcpToToolServices){
		this.agentPromptProvider = agentPromptProvider;
		this.chatMemoryRepository = chatMemoryRepository;
		this.chatModel = chatModel;
		this.serverProperties = serverProperties;
		this.toolManager = toolManager;
		this.pushNotificationSenderService = pushNotificationSenderService;
		this.observationRegistry = observationRegistry;
		this.mcpToToolServices = mcpToToolServices;
	}

	/**
	 * 构造 map 对话客户端
	 * @param chatModels
	 * @return
	 */
	@Bean
	public Map<String, ChatClient> chatClients(List<ChatModel> chatModels) {
		return chatModels.stream().collect(Collectors.toMap(model -> model.getClass().getSimpleName().toLowerCase(),
				model -> ChatClient.builder(model).build()));

	}

	/**
	 * 构造hostAgent
	 *
	 * @return
	 */
	@Bean
	public McpHostAgent mcpHostAgent() {
		var hostAgent = McpHostAgent.builder()
				.observationRegistry(this.observationRegistry)
				.agentPromptProvider(this.agentPromptProvider)
				.chatMemoryRepository(this.chatMemoryRepository)
				.chatModel(this.chatModel)
				.a2aServerProperties(this.serverProperties)
				.toolManager(this.toolManager)
				.mcpToToolServices(this.mcpToToolServices)
				.build();
		return hostAgent;
	}

	@Bean
	public MyTaskManager taskManager() {
		return new MyTaskManager(mcpHostAgent(), this.pushNotificationSenderService,this.serverProperties);
	}


}
