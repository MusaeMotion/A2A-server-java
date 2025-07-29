package com.musaemotion.a2a.agent.server.mcp.filter;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.function.BiPredicate;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/23  18:07
 * @description 工具过滤逻辑, 适合权限和其他判断控制
 */
public interface  IToolFilter {

	/**
	 * McpAsyncClient 过滤
	 * @return
	 */
	BiPredicate<McpAsyncClient, McpSchema.Tool> mcpAsyncClientToolFilter();

	/**
	 * McpSyncClient 过滤
	 * @return
	 */
	 BiPredicate<McpSyncClient, McpSchema.Tool> mcpSyncClientToolFilter();
}
