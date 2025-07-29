package com.musaemotion.a2a.agent.server.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.musaemotion.a2a.agent.server.mcp.constant.McpConnType;
import com.musaemotion.a2a.agent.server.mcp.constant.McpSseClientType;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/22  16:16
 * @description
 */
public sealed interface ConfigMarker
		permits StdioConfig, SseConfig {

	/**
	 * 链接方式
	 * @return
	 */
	@JsonIgnore
	McpConnType getConnType();

	/**
	 * 客户端 异步还是同步
	 * @return
	 */
	McpSseClientType getClientType();
}
