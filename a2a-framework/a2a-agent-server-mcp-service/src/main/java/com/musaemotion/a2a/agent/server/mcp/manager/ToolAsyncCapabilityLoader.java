package com.musaemotion.a2a.agent.server.mcp.manager;

import com.musaemotion.a2a.agent.server.mcp.model.McpBasics;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/24  15:36
 * @description
 */
public class ToolAsyncCapabilityLoader extends ToolCapabilityLoader {
	private final McpAsyncClient mcpAsyncClient;

	public ToolAsyncCapabilityLoader(McpBasics mcpBasics, McpSchema.ServerCapabilities serverCapabilities, McpAsyncClient mcpAsyncClient) {
		super(mcpBasics, serverCapabilities);
		this.mcpAsyncClient = mcpAsyncClient;
	}

	@Override
	public void loadTools() {
		this.loadTools(new Supplier<List<McpSchema.Tool>>() {
			@Override
			public List<McpSchema.Tool> get() {
				// 直接调用 tools工具获取
				return mcpAsyncClient.listTools().block(Duration.ofMillis(5)).tools();
			}
		});
	}

	@Override
	public void loadResources() {
		this.loadResources(new Supplier<List<McpSchema.Resource>>() {
			@Override
			public List<McpSchema.Resource> get() {
				return mcpAsyncClient.listResources().block(Duration.ofMillis(5)).resources();
			}
		});
	}

	@Override
	public void loadPrompts() {
		this.loadPrompts(new Supplier<List<McpSchema.Prompt>>() {
			@Override
			public List<McpSchema.Prompt> get() {
				return mcpAsyncClient.listPrompts().block(Duration.ofMillis(5)).prompts();
			}
		});
	}
}
