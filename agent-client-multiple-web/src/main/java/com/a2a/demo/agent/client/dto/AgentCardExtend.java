package com.a2a.demo.agent.client.dto;

import com.musaemotion.a2a.common.AgentCard;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.util.function.Function;

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

	/**
	 * 智能体提示词
	 */
	private String agentPrompt;

	/**
	 * 构造提示词
	 * @param function
	 * @return
	 */
	public AgentCardExtend buildPrompt(Function<String , String> function) {
		if(this.getCapabilities().modifyPrompt()) {
			this.setAgentPrompt(
					function.apply(this.getName())
			);
		}
		return this;
	}

	public AgentCard toAgentCard(){
		AgentCard agentCard = new AgentCard();
		BeanUtils.copyProperties(this, agentCard);
		return agentCard;
	}

}
