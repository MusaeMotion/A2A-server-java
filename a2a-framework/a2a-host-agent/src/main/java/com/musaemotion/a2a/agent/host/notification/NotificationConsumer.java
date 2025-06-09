package com.musaemotion.a2a.agent.host.notification;

import com.musaemotion.a2a.agent.client.INotificationConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/7  00:38
 * @description 默认的 消息消费者
 */
@Service
@Slf4j
public class NotificationConsumer implements INotificationConsumer {

	private ApplicationEventPublisher publisher;

	@Autowired
	public NotificationConsumer(ApplicationEventPublisher publisher){
		this.publisher = publisher;
	}

	@Override
	public void processMessage(String message) {
        // log.info(message);
		publisher.publishEvent(message);
	}
}
