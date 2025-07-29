package com.musaemotion.a2a.agent.server.mcp.model;

import com.musaemotion.a2a.agent.server.mcp.constant.McpConnType;
import lombok.Data;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/21  15:24
 * @description 搜索Mcp配置
 */
@Data
public class SearchMcpConfig {

	/**
	 * mcp名称
	 */
	private String name;

	/**
	 * mcp 名称
	 */
	private McpConnType connType;

	/**
	 * 状态
	 */
	private Boolean enable;
}
