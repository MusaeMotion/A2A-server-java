package com.musaemotion.a2a.agent.server.mcp.manager;

import com.musaemotion.a2a.agent.server.mcp.model.McpBasics;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/24  15:38
 * @description
 */
public class ToolSyncCapabilityLoader extends ToolCapabilityLoader {

	private final McpSyncClient mcpSyncClient;

	public ToolSyncCapabilityLoader(McpBasics mcpBasics, McpSchema.ServerCapabilities serverCapabilities, McpSyncClient mcpSyncClient) {
		super(mcpBasics, serverCapabilities);
		this.mcpSyncClient = mcpSyncClient;
	}

	@Override
	public void loadTools() {
		this.loadTools(new Supplier<List<McpSchema.Tool>>() {
			@Override
			public List<McpSchema.Tool> get() {
				// 直接调用 tools工具获取
				return mcpSyncClient.listTools().tools();
			}
		});
	}

	@Override
	public void loadResources() {
		this.loadResources(new Supplier<List<McpSchema.Resource>>() {
			@Override
			public List<McpSchema.Resource> get() {
				return mcpSyncClient.listResources().resources();
			}
		});
	}

	@Override
	public void loadPrompts() {
		this.loadPrompts(new Supplier<List<McpSchema.Prompt>>() {
			@Override
			public List<McpSchema.Prompt> get() {
				return mcpSyncClient.listPrompts().prompts();
			}
		});
	}
}
