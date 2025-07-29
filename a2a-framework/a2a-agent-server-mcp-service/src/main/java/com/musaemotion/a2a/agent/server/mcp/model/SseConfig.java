package com.musaemotion.a2a.agent.server.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.musaemotion.a2a.agent.server.mcp.constant.McpConnType;
import com.musaemotion.a2a.agent.server.mcp.constant.McpSseClientType;
import lombok.*;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/21  14:48
 * @description sse模式配置文件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class SseConfig implements ConfigMarker {

	/**
	 * url
	 */
	private String url;

	/**
	 * 终结点, 访问相对路径
	 */
	@Builder.Default
	private String sseEndpoint = "/sse";

	/**
	 *
	 */
	@Builder.Default
	@Getter
	private McpSseClientType clientType =  McpSseClientType.SYNC;


	@JsonIgnore
	@Override
	public McpConnType getConnType() {
		return McpConnType.SSE;
	}


}
