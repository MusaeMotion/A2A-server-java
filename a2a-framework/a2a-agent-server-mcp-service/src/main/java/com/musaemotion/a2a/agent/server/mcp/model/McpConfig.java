package com.musaemotion.a2a.agent.server.mcp.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.musaemotion.a2a.agent.server.mcp.constant.McpConnType;
import com.musaemotion.a2a.agent.server.mcp.constant.McpSseClientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/21  14:59
 * @description mcp配置
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class McpConfig<T extends ConfigMarker> {

	private String name;

	private String description;

	@JsonTypeInfo(
			use = JsonTypeInfo.Id.NAME,         // 用名字区分
			include = JsonTypeInfo.As.EXTERNAL_PROPERTY, // 与 connType 同级
			property = "connType")              // 根据 connType 的值找子类
	@JsonSubTypes({
			@JsonSubTypes.Type(value = StdioConfig.class, name = "STDIO"),
			@JsonSubTypes.Type(value = SseConfig.class,   name = "SSE")
	})
	private T config;

	/**
	 * 获取类型
	 * @return
	 */
	public McpConnType getConnType() {
		return this.config.getConnType();
	}


}
