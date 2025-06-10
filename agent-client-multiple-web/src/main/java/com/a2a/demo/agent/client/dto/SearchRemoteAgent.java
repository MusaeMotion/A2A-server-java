package com.a2a.demo.agent.client.dto;

import lombok.Data;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/10  10:19
 * @description 搜索扩展
 */
@Data
public class SearchRemoteAgent {

	/**
	 * 智能体名称
	 */
	private String name;

	/**
	 * 智能体描述
	 */
	private String description;

	/**
	 * 是否启用
	 */
	private Boolean enable;
}
