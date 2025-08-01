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

import com.musaemotion.a2a.common.base.*;
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
public class CommonMessageExt extends Common.Message implements CalculateChargeable {

	/**
	 * 相关任务
	 */
    private List<Task> task;

	// 计算委托
	private CalculateChargeableDelegate calculateChargeableDelegate;

	// 构建
	public CommonMessageExt() {
		this.calculateChargeableDelegate = new CalculateChargeableDelegate(this);
	}
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
	public void setCompletionTokensAmount(BigDecimal amount){
		this.calculateChargeableDelegate.setCompletionTokensAmount(amount);
	}

	/**
	 * 设置输入金额
	 */
	public void setPromptTokensAmount(BigDecimal amount){
		this.calculateChargeableDelegate.setPromptTokensAmount(amount);
	}

	/**
	 * 设置总金额
	 * @param amount
	 */
	public void setTotalAmount(BigDecimal amount){
		this.calculateChargeableDelegate.setTotalAmount(amount);
	}
	/**
	 * 获取输出金额
	 * @return
	 */
	public BigDecimal getCompletionTokensAmount(){
		return this.calculateChargeableDelegate.getCompletionTokensAmount();
	}

	/**
	 * 获取输入金额
	 * @return
	 */
	public BigDecimal getPromptTokensAmount(){
		return this.calculateChargeableDelegate.getPromptTokensAmount();
	}

	/**
	 * 输出token
	 * @return
	 */
	public Integer getCompletionTokens(){
		return this.calculateChargeableDelegate.getCompletionTokens();
	}


	/**
	 * 输入token
	 * @return
	 */
	public Integer getPromptTokens(){
		return this.calculateChargeableDelegate.getPromptTokens();
	}


	/**
	 * 获取消耗次数
	 * @return
	 */
	public Integer getFrequency(){
		return this.calculateChargeableDelegate.getFrequency();
	}

	/**
	 * 获取模型名称
	 * @return
	 */
	public String getModelName(){
		return this.calculateChargeableDelegate.getModelName();
	}


	/**
	 * 计算金额
	 * @param calculateAmount
	 */
	public void calAmount(CalculateAmount calculateAmount) {
		if(this.getRole().equals(USER)){
			return;
		}
		if (!StringUtils.hasText(this.calculateChargeableDelegate.getModelName())) {
			log.warn("calAmount: hostAgent 没有找到模型名称：{}", this.calculateChargeableDelegate.getModelName());
			this.calculateChargeableDelegate.setCompletionTokensAmount(BigDecimal.ZERO);
			this.calculateChargeableDelegate.setPromptTokensAmount(BigDecimal.ZERO);
			return;
		}
		if (this.getTask() != null) {
			this.getTask().forEach(task -> {
				task.calAmount(calculateAmount);
			});
		}
		this.calculateChargeableDelegate.calAmount(calculateAmount);
	}
}
