/*
 * Copyright (c) 2025 MusaeMotion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.musaemotion.a2a.agent.host.model.response;

import com.musaemotion.a2a.common.base.CalculateAmount;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.common.constant.MetaDataKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.musaemotion.a2a.common.constant.MessageRole.USER;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.model.response
 * @project：A2A
 * @date：2025/5/15 16:29
 * @description：请完善描述
 */
@Data
@EqualsAndHashCode(callSuper =  true)
@Slf4j
public class CommonMessageExt extends Common.Message {

	/**
	 * 相关任务
	 */
    private List<Task> task;

	/**
	 *
	 * @param message
	 * @return
	 */
    public static CommonMessageExt fromMessage(Common.Message message) {
        CommonMessageExt commonMessageExt = new CommonMessageExt();
        BeanUtils.copyProperties(message, commonMessageExt);
        return commonMessageExt;
    }



	/**
	 * 设置输出金额
	 */
	private void setCompletionTokensAmount(BigDecimal completionTokensAmount){
		this.getMetadata().put(
				MetaDataKey.COMPLETION_TOKENS_AMOUNT,
				completionTokensAmount
		);
	}

	/**
	 * 设置输入金额
	 */
	private void setPromptTokensAmount(BigDecimal promptTokensAmount){
		this.getMetadata().put(
				MetaDataKey.PROMPT_TOKENS_AMOUNT,
				promptTokensAmount
		);
	}

	/**
	 * 设置总金额
	 * @param totalAmount
	 */
	private void setTotalAmount(BigDecimal totalAmount){
		this.getMetadata().put(
				MetaDataKey.TOTAL_AMOUNT,
				totalAmount
		);
	}
	/**
	 * 获取输出金额
	 * @return
	 */
	private BigDecimal getCompletionTokensAmount(){
		if(this.getMetadata().containsKey(MetaDataKey.COMPLETION_TOKENS_AMOUNT)){
			return (BigDecimal)this.getMetadata().get(MetaDataKey.COMPLETION_TOKENS_AMOUNT);
		}
		return BigDecimal.ZERO;
	}

	/**
	 * 获取输入金额
	 * @return
	 */
	private BigDecimal getPromptTokensAmount(){
		if(this.getMetadata().containsKey(MetaDataKey.PROMPT_TOKENS_AMOUNT)){
			return (BigDecimal)this.getMetadata().get(MetaDataKey.PROMPT_TOKENS_AMOUNT);
		}
		return BigDecimal.ZERO;
	}

	/**
	 * 输出token
	 * @return
	 */
	private Integer getCompletionTokens(){
		if(this.getMetadata().containsKey(MetaDataKey.COMPLETION_TOKENS)){
			return (Integer)this.getMetadata().get(MetaDataKey.COMPLETION_TOKENS);
		}
		return 0;
	}

	/**
	 * 输入token
	 * @return
	 */
	private Integer getPromptTokens(){
		if(this.getMetadata().containsKey(MetaDataKey.PROMPT_TOKENS)){
			return (Integer)this.getMetadata().get(MetaDataKey.PROMPT_TOKENS);
		}
		return 0;
	}


	/**
	 * 获取消耗次数
	 * @return
	 */
	private Integer getFrequency(){
		if(this.getMetadata().containsKey(MetaDataKey.FREQUENCY)){
			return (Integer)this.getMetadata().get(MetaDataKey.FREQUENCY);
		}
		return 0;
	}

	/**
	 * 获取模型名称
	 * @return
	 */
	private String getModelName(){
		if(this.getMetadata().containsKey(MetaDataKey.USE_MODEL)){
			return (String)this.getMetadata().get(MetaDataKey.USE_MODEL);
		}
		return "";
	}


	/**
	 * 计算
	 * @param calculateAmount
	 */
	public void calAmount(CalculateAmount calculateAmount) {
		if(this.getRole().equals(USER)){
			return;
		}
		if (!StringUtils.hasText(this.getModelName())) {
			log.warn("calAmount: hostAgent 没有找到模型名称：{}", this.getModelName());
			this.setCompletionTokensAmount(BigDecimal.ZERO);
			this.setPromptTokensAmount(BigDecimal.ZERO);
			return;
		}
		if (this.getTask() != null) {
			this.getTask().forEach(task -> {
				task.calAmount(calculateAmount);
			});
		}
		if (this.getFrequency() > 0) {
			this.setTotalAmount(calculateAmount.calculateCallAmount(this.getFrequency(), this.getModelName()));
			return;
		}
		this.setCompletionTokensAmount(calculateAmount.calculateUsageCompletionAmount(this.getCompletionTokens(), this.getModelName()));
		this.setPromptTokensAmount(calculateAmount.calculateUsagePromptAmount(this.getPromptTokens(), this.getModelName()));
		this.setTotalAmount(this.getCompletionTokensAmount().add(this.getPromptTokensAmount()));
	}
}
