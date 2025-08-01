package com.musaemotion.a2a.agent.server.mcp.core;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.customizer.McpAsyncClientCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/18  18:23
 * @description 异步自定以实现
 */
@Component
@Slf4j
public class CustomMcpAsyncClientCustomizer implements McpAsyncClientCustomizer {


	// private List<McpSchema.Root> roots = Lists.newArrayList();

	private Map<String, ChatClient> chatClients;

	@Autowired
	public CustomMcpAsyncClientCustomizer(Map<String, ChatClient> chatClients) {
		this.chatClients = chatClients;
	}


	@Override
	public void customize(String serverConfigurationName, McpClient.AsyncSpec spec) {
		// Customize the async client configuration
		spec.requestTimeout(Duration.ofSeconds(30));

		// 设置此客户端可以访问的根URI。
		// spec.roots(roots);

		// 设置用于处理消息创建请求的自定义采样处理程序。
		spec.sampling((McpSchema.CreateMessageRequest llmRequest) -> {
			// Handle sampling
			log.debug("llmRequest: {}", llmRequest);
			var userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
			String modelHint = llmRequest.modelPreferences().hints().get(0).name();

			// 根据型号提示找到合适的聊天客户端
			ChatClient hintedChatClient = chatClients.entrySet().stream()
					.filter(e -> e.getKey().contains(modelHint)).findFirst()
					.orElseThrow().getValue();

			// Generate response using the selected model
			String response = hintedChatClient.prompt()
					.system(llmRequest.systemPrompt())
					.user(userPrompt)
					.call()
					.content();

			Mono<McpSchema.CreateMessageResult> createMessageResult = Mono.just(McpSchema.CreateMessageResult.builder().content(new McpSchema.TextContent(response)).build());
			return createMessageResult;
		});

		// 添加一个消费者，以便在可用工具（如工具）发生变化时收到通知
		// 被添加或删除。
		spec.toolsChangeConsumer((List<McpSchema.Tool> tools) -> {

			// 示例：异步保存工具变更到数据库
			return Mono.fromRunnable(() -> {
				// Handle tools change
				log.debug("toolsChangeConsumer: {}", tools);
				// 可以调用 reactive repository 或其他异步操作
			}).then(); // 返回
		});

		// 添加一个消费者，以便在可用资源发生变化时收到通知，例如资源
		// 被添加或删除。
		spec.resourcesChangeConsumer((List<McpSchema.Resource> resources) -> {
			// Handle resources change
			// 示例：异步保存工具变更到数据库
			return Mono.fromRunnable(() -> {
				// Handle tools change
				log.debug("resourcesChangeConsumer: {}", resources);
				// 可以调用 reactive repository 或其他异步操作
			}).then(); // 返回
		});

		// 添加在可用提示（如提示）更改时要通知的消费者
		// 被添加或删除。
		spec.promptsChangeConsumer((List<McpSchema.Prompt> prompts) -> {
			// 示例：异步保存工具变更到数据库
			return Mono.fromRunnable(() -> {
				// Handle tools change
				log.debug("promptsChangeConsumer: {}", prompts);
				// 可以调用 reactive repository 或其他异步操作
			}).then(); // 返回
		});

		// 添加在从服务器收到日志消息时要通知的消费者。
		spec.loggingConsumer((McpSchema.LoggingMessageNotification logMsg) -> {
			// 示例：异步保存工具变更到数据库
			return Mono.fromRunnable(() -> {
				// Handle tools change
				log.debug("loggingConsumer: {}", logMsg);
				// 可以调用 reactive repository 或其他异步操作
			}).then(); // 返回
		});
	}
}
