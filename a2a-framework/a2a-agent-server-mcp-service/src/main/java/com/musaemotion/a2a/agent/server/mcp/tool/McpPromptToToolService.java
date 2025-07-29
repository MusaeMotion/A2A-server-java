package com.musaemotion.a2a.agent.server.mcp.tool;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/24  10:22
 * @description mcp 转 工具服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class McpPromptToToolService extends AbstractMcpToToolService {


	/**
	 * @param mcpName
	 * @param promptName
	 * @param promptArg
	 * @param toolContext
	 * @return
	 */
	@Tool(description = """
				生成提示词
				Args:
				  mcpName: 需要读取的资源所属的mcp名称，类型为String
			      promptName: 对应的 prompts 的名称
			      promptArg: 对应的 prompts 相关的参数
				Yields:
				  A dictionary of JSON data.
			""")
	public McpSchema.GetPromptResult generatePrompt(String mcpName, String promptName, Map<String, Object> promptArg, ToolContext toolContext) {
		log.warn("=============================McpPromptToToolService {}, {}, {}", mcpName, promptName, promptArg);
		var result = this.clientCallFunction(mcpName, new BiFunction<String, Object, McpSchema.GetPromptResult>() {
			@Override
			public McpSchema.GetPromptResult apply(String s, Object o) {
				if (o instanceof McpAsyncClient mcpAsyncClient) {
					return mcpAsyncClient.getPrompt(
									new McpSchema.GetPromptRequest(promptName, promptArg))
							.block(Duration.ofMillis(5));
				}
				if (o instanceof McpSyncClient mcpSyncClient) {
					return mcpSyncClient.getPrompt(
							new McpSchema.GetPromptRequest(promptName, promptArg));
				}
				return null;
			}
		});
		log.warn("===========================McpPromptToToolService result {} ", result.toString());
		return result;
	}
}
