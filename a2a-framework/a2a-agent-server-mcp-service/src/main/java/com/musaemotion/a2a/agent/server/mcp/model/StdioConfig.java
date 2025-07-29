package com.musaemotion.a2a.agent.server.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.musaemotion.a2a.agent.server.mcp.constant.McpConnType;
import com.musaemotion.a2a.agent.server.mcp.constant.McpSseClientType;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/21  14:45
 * @description 标准输入输出配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final  class StdioConfig  implements ConfigMarker {
	/**
	 * 命令
	 */
	private String command;
	/**
	 * 参数
	 */
	private List<String> args;

	/**
	 * 环境变量
	 */
	private Map<String, String> env;

	/**
	 *
	 */
	@Builder.Default
	@Getter
	private McpSseClientType clientType =  McpSseClientType.SYNC;

	@JsonIgnore
	@Override
	public McpConnType getConnType() {
		return McpConnType.STDIO;
	}
}
