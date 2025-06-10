package com.a2a.demo.agent.client.dto;

import com.musaemotion.a2a.common.AgentCard;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/10  09:25
 * @description 智能体卡扩展
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentCardExtend extends AgentCard {

	/**
	 * id
	 */
	private String id;

	/**
	 * 是否启用
	 */
	private Boolean enable;

}
