package com.musaemotion.a2a.agent.host.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/20  17:30
 * @description 智能体运行流对象
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentRunningStreamModel {

	/**
	 * 文本内容
	 */
	private String text;

	/**
	 * 任务id
	 */
	private String taskId;

	/**
	 * 其他信息
	 */
	private Map<String, Object> metadata;
}
