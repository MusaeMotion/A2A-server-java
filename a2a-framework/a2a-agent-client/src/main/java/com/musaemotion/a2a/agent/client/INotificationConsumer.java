package com.musaemotion.a2a.agent.client;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/7  00:09
 * @description 消息消费者
 */
public interface INotificationConsumer {

	/**
	 *
	 * @param message
	 * @param agentName
	 */
	void processMessage(String message,String agentName);
}
