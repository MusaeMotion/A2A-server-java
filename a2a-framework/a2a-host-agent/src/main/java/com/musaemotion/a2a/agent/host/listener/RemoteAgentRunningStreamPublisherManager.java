package com.musaemotion.a2a.agent.host.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/20  16:23
 * @description 运行流监听管理者
 */
@Slf4j
public class RemoteAgentRunningStreamPublisherManager implements RemoteAgentRunningStreamPublisher {


	private List<RemoteAgentRunningStreamPublisher> runningStreamListeners;

	public RemoteAgentRunningStreamPublisherManager(List<RemoteAgentRunningStreamPublisher> runningStreamListeners) {
		this.runningStreamListeners = runningStreamListeners;
	}

	/**
	 * 监听消息
	 * @param text
	 * @param taskId
	 * @param metadata
	 */
	@Override
	public void publisher(String text, String taskId, Map<String, Object> metadata) {
		this.runningStreamListeners.stream().forEach((runningStreamListener) -> {
				runningStreamListener.publisher(text, taskId, metadata);
		});
	}
}
