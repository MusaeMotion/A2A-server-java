package com.musaemotion.a2a.agent.host.notification;

import com.musaemotion.a2a.agent.client.INotificationConsumer;
import com.musaemotion.a2a.agent.host.constant.AppEventType;
import com.musaemotion.a2a.agent.host.event.AgentAppEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/7  00:38
 * @description 默认的 消息消费者
 */
@Service
@Slf4j
public class DefaultNotificationConsumer implements INotificationConsumer {

	/**
	 * spring ApplicationEventPublisher
	 */
	private ApplicationEventPublisher publisher;

	@Autowired
	public DefaultNotificationConsumer(ApplicationEventPublisher publisher){
		this.publisher = publisher;
	}

	@Override
	public void processMessage(String message,String agentName) {
		log.info("Message received: {} ", message);
		AgentAppEvent agentAppEvent = new AgentAppEvent(this, message, AppEventType.NOTIFICATION, agentName);
		this.publisher.publishEvent(agentAppEvent);
	}
}
