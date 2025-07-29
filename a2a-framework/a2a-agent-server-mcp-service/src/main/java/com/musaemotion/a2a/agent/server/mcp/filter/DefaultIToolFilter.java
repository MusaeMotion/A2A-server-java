package com.musaemotion.a2a.agent.server.mcp.filter;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.function.BiPredicate;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/23  18:11
 * @description 默认过滤逻辑
 */
public class DefaultIToolFilter implements IToolFilter {
	@Override
	public BiPredicate<McpAsyncClient, McpSchema.Tool> mcpAsyncClientToolFilter() {
		return new BiPredicate<McpAsyncClient, McpSchema.Tool>() {
			@Override
			public boolean test(McpAsyncClient mcpAsyncClient, McpSchema.Tool tool) {
				return true;
			}
		};
	}

	@Override
	public BiPredicate<McpSyncClient, McpSchema.Tool> mcpSyncClientToolFilter() {
		return new BiPredicate<McpSyncClient, McpSchema.Tool>() {
			@Override
			public boolean test(McpSyncClient mcpAsyncClient, McpSchema.Tool tool) {
				return true;
			}
		};
	}
}
