package com.musaemotion.a2a.agent.server.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/25  13:05
 * @description 工具基础说明
 */
@Data
@Builder
public class McpBasics {

	/**
	 * mcp 名称
	 */
	private String mcpName;

	/**
	 * mcp 描述
	 */
	private String mcpDescription;

	/**
	 * 相关资源
	 */
	@JsonIgnore
	private List<McpSchema.Resource> resources;

	/**
	 * 资源模板
	 */
	@JsonIgnore
	private List<McpSchema.ResourceTemplate> resourceTemplates;

	/**
	 * 工具
	 */
	private List<McpSchema.Tool> tools;

	/**
	 * 提示词
	 */
	@JsonIgnore
	private List<McpSchema.Prompt> prompts;


}
