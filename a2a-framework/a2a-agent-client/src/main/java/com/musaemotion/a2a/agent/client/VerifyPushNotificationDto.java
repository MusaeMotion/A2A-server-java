package com.musaemotion.a2a.agent.client;

import lombok.Builder;
import lombok.Data;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/23  13:22
 * @description 消息验证返回内容
 */
@Data
@Builder
public class VerifyPushNotificationDto {
	/**
	 * 是否验签通过
	 */
	@Builder.Default
	private Boolean success = false;
	/**
	 * 智能体名称
	 */
	private String agentName;
}
