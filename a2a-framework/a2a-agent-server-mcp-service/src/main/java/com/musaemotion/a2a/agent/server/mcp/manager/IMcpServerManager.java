package com.musaemotion.a2a.agent.server.mcp.manager;

import com.musaemotion.a2a.agent.server.mcp.model.McpConfig;
import com.musaemotion.a2a.agent.server.mcp.model.SearchMcpConfig;

import java.util.List;
import java.util.Optional;


/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/18  17:50
 * @description mcp 服务管理器
 */
public interface IMcpServerManager<S extends SearchMcpConfig> {

	/**
	 * 根据mcp名称获取配置文件
	 * @param name
	 * @return
	 */
	Optional<McpConfig<?>> getMcpConfig(String name);

	/**
	 * 获取所有配置
	 * @return
	 */
	List<McpConfig<?>> searchMcpConfigs(SearchMcpConfig searchInput);


}
