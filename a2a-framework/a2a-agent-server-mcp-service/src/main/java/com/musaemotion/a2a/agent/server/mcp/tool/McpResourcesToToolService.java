package com.musaemotion.a2a.agent.server.mcp.tool;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.BiFunction;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/23  18:27
 * @description 工具方法
 */
@Service
@Slf4j
public class McpResourcesToToolService extends AbstractMcpToToolService {


	/**
	 * 返回
	 * @param mcpName
	 * @param resourceUri
	 * @param toolContext
	 * @return
	 */
	@Tool(description = """
		读取资源工具
		Args:
		  mcpName: 需要读取的资源所属的mcp名称，类型为String
	      resourceUri: 读取的资源uri
		Yields:
		  A dictionary of JSON data.
	""")
	public McpSchema.ReadResourceResult readResource(String mcpName, String resourceUri, ToolContext toolContext) {
		log.warn("===================================McpResourcesToToolService {}, {}, {}", mcpName, resourceUri);
		var result = this.clientCallFunction(mcpName, new BiFunction<String, Object, McpSchema.ReadResourceResult>() {
			@Override
			public McpSchema.ReadResourceResult apply(String s, Object o) {
				if(o instanceof McpAsyncClient mcpAsyncClient) {
					return mcpAsyncClient.readResource(new McpSchema.ReadResourceRequest(resourceUri)).block(Duration.ofMillis(5));
				}
				if(o instanceof McpSyncClient mcpSyncClient) {
					return mcpSyncClient.readResource(new McpSchema.ReadResourceRequest(resourceUri));
				}
				return null;
			}
		});
		log.warn("==================================McpResourcesToToolService result {} ", result.toString());
		return result;
	}
}
