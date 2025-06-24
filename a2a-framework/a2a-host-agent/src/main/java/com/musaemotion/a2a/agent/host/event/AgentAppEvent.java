package com.musaemotion.a2a.agent.host.event;

import com.musaemotion.a2a.agent.host.constant.AppEventType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/20  17:20
 * @description 远程智能体运行事件
 */

public class AgentAppEvent extends ApplicationEvent {

	@Getter
	private String message;

	@Getter
	private AppEventType eventType;

	/**
	 * 智能体名称
	 */
	@Getter
	private String agentName;

	public AgentAppEvent(Object source, String message, AppEventType eventType, String agentName) {
		super(source);
		this.message = message;
		this.eventType = eventType;
		this.agentName = agentName;
	}
}
