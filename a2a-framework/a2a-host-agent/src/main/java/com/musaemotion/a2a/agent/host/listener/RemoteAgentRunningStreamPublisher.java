package com.musaemotion.a2a.agent.host.listener;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/20  16:02
 * @description 远程智能体运行中流监听
 */
public interface RemoteAgentRunningStreamPublisher {
	/**
	 * 监听消息
	 * @param text
	 * @param taskId
	 * @param taskSendMetadata
	 */
	void publisher(String text, String taskId, Map<String, Object> taskSendMetadata);
}
