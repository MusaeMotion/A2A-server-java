package com.musaemotion.a2a.agent.server.mcp.tool;

import com.musaemotion.a2a.agent.server.mcp.manager.McpClientManager;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/23  18:26
 * @description 抽象发现工具
 */
public abstract class AbstractMcpToToolService {

	/**
	 * mcp Client 管理器
	 */
	@Autowired
	protected McpClientManager mcpClientManager;

	public ToolCallback[] getToolCallback(){
		return ToolCallbacks.from(this);
	}

	/**
	 * 获取Async
	 * @param mcpName
	 * @return
	 */
	protected <T> T clientCallFunction(String mcpName, BiFunction<String, Object, T> biFunction) {
		Optional<McpAsyncClient> optionalAsync = this.mcpClientManager.getAsync(mcpName);
		if (optionalAsync.isPresent()) {
			return biFunction.apply(mcpName, optionalAsync.get());
		}
		Optional<McpSyncClient> optionalSync = this.mcpClientManager.getSync(mcpName);
		if (optionalSync.isPresent()) {
			return biFunction.apply(mcpName, optionalSync.get());
		}
		throw new IllegalArgumentException("No client found for MCP: " + mcpName);
	}


}
