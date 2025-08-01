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

package com.musaemotion.a2a.common.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.common.constant.MetaDataKey;
import com.musaemotion.a2a.common.constant.TaskState;
import com.musaemotion.a2a.common.event.AbstractTask;
import com.musaemotion.a2a.common.event.TaskArtifactUpdateEvent;
import com.musaemotion.a2a.common.event.TaskEvent;
import com.musaemotion.a2a.common.event.TaskStatusUpdateEvent;
import com.musaemotion.a2a.common.request.params.TaskSendParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.musaemotion.a2a.common.constant.MetaDataKey.*;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper=true)
@Slf4j
public class Task extends AbstractTask implements CalculateChargeable {


	// sessionId
	private String sessionId;

	// 返回的信息里面主要是先看状态字段
	private Common.TaskStatus status;

	// 返回的工件
	private List<Common.Artifact> artifacts;

	// 历史记录
	private List<Common.Message> history;

	// 计算委托
	@JsonIgnore
	private CalculateChargeableDelegate calculateChargeableDelegate;

	// 构建
	public Task() {
		this.calculateChargeableDelegate = new CalculateChargeableDelegate(this);
	}

	/**
	 * @param task
	 * @param taskStatusUpdateEvent
	 * @return
	 */
	public static Task statusUpdateFrom(Task task, TaskStatusUpdateEvent taskStatusUpdateEvent) {
		Task taskStatusUpdate = task;
		if (taskStatusUpdate == null) {
			taskStatusUpdate = new Task();
		}
		taskStatusUpdate.setId(taskStatusUpdateEvent.getId());
		taskStatusUpdate.setStatus(taskStatusUpdateEvent.getStatus());
		taskStatusUpdate.setMetadata(taskStatusUpdateEvent.getMetadata());
		return taskStatusUpdate;
	}

	/**
	 * 根据产出时间包装任务
	 * @param taskArtifactUpdateEvent
	 * @return
	 */
	public static Task updateTaskFromArtifactUpdateEvent(Task task, TaskArtifactUpdateEvent taskArtifactUpdateEvent) {
		task.setId(taskArtifactUpdateEvent.getId());
		task.setArtifacts(Lists.newArrayList(taskArtifactUpdateEvent.getArtifact()));
		task.setMetadata(taskArtifactUpdateEvent.getMetadata());
		return task;
	}

	/**
	 * 根据任务请求参数包装任务
	 *
	 * @param taskSendParams
	 * @return
	 */
	public static Task from(TaskSendParams taskSendParams, TaskState taskState) {
		Task task = new Task();
		task.setId(taskSendParams.getId());
		task.setSessionId(taskSendParams.getSessionId());
		task.setStatus(
				Common.TaskStatus.builder()
						.state(taskState)
						.message(taskSendParams.getMessage())
						.build()
		);
		task.setHistory(Lists.newArrayList(taskSendParams.getMessage()));
		task.setMetadata(taskSendParams.getMetadata());
		return task;
	}

	/**
	 * 根据任务事件构建Task
	 *
	 * @param taskEvent
	 * @return
	 */
	public static Task buildSubmittedFrom(TaskEvent taskEvent) {
		Task task = new Task();
		task.setId(taskEvent.getId());
		task.setStatus(Common.TaskStatus.builder().state(TaskState.SUBMITTED).build());
		task.setMetadata(taskEvent.getMetadata());
		task.setArtifacts(Lists.newArrayList());
		String conversationId = null;
		if (taskEvent.getMetadata() != null && taskEvent.getMetadata().containsKey(CONVERSATION_ID)) {
			conversationId = taskEvent.getMetadata().get(CONVERSATION_ID).toString();
		}
		task.setSessionId(conversationId);
		return task;
	}

	/**
	 * 拷贝一个消息对象
	 */
	public Task copyNotification() {
		Task task = new Task();
		task.setId(this.getId());
		task.setSessionId(this.getSessionId());
		task.setMetadata(this.getMetadata());
		// 清空工件
		task.setArtifacts(Lists.newArrayList());
		// 历史记录清空
		task.setHistory(Lists.newArrayList());


		if (this.getStatus() != null) {
			var taskStatusBuilder = Common.TaskStatus.builder()
					.state(this.getStatus().getState())
					.timestamp(this.getStatus().getTimestamp());
			if (this.getStatus().getMessage() != null) {
				taskStatusBuilder.message(
						Common.Message.newMessage(
								this.getStatus().getMessage().getRole(),
								Lists.newArrayList(),
								this.getStatus().getMessage().getMetadata()
						)
				);
			}
			task.setStatus(taskStatusBuilder.build());
		}

		return task;
	}

	/**
	 * 用户输入消息id
	 *
	 * @return
	 */
	@JsonIgnore
	public String getInputMessageId() {
		if (this.metadata == null) {
			return null;
		}
		if (!this.metadata.containsKey(INPUT_MESSAGE_ID)) {
			return null;
		}
		return this.metadata.get(INPUT_MESSAGE_ID).toString();
	}

	/**
	 * 智能体交互消息id
	 *
	 * @return
	 */
	@JsonIgnore
	public String getMessageId() {
		if (this.metadata == null) {
			return null;
		}
		if (!this.metadata.containsKey(MESSAGE_ID)) {
			return null;
		}
		return this.metadata.get(MESSAGE_ID).toString();
	}

	/**
	 * 获取消耗次数
	 *
	 * @return
	 */
	@JsonIgnore
	public Integer getFrequency() {
		return this.calculateChargeableDelegate.getFrequency();
	}

	/**
	 * 获取所有消耗的tokens
	 *
	 * @return
	 */
	@JsonIgnore
	public Integer getTotalTokens() {
		if (this.getMetadata().containsKey(MetaDataKey.TOTAL_TOKENS)) {
			return (Integer) this.getMetadata().get(MetaDataKey.TOTAL_TOKENS);
		}
		return 0;
	}

	/**
	 * 获取模型名称
	 *
	 * @return
	 */
	@JsonIgnore
	public String getModelName() {
		return this.calculateChargeableDelegate.getModelName();
	}

	/**
	 * 输出token
	 *
	 * @return
	 */
	@JsonIgnore
	public Integer getCompletionTokens() {
		return this.calculateChargeableDelegate.getCompletionTokens();
	}

	/**
	 * 输入token
	 *
	 * @return
	 */
	@JsonIgnore
	public Integer getPromptTokens() {
		return this.calculateChargeableDelegate.getPromptTokens();
	}

	/**
	 * 设置输出金额
	 */
	public void setCompletionTokensAmount(BigDecimal amount) {
		this.calculateChargeableDelegate.setCompletionTokensAmount(amount);
	}

	/**
	 * 设置输入金额
	 */
	public void setPromptTokensAmount(BigDecimal amount) {
		this.calculateChargeableDelegate.setPromptTokensAmount(amount);
	}

	/**
	 * 设置总金额
	 *
	 * @param amount
	 */
	public void setTotalAmount(BigDecimal amount) {
		this.calculateChargeableDelegate.setTotalAmount(amount);
	}

	/**
	 * 获取输出金额
	 *
	 * @return
	 */
	public BigDecimal getCompletionTokensAmount() {
		return this.calculateChargeableDelegate.getCompletionTokensAmount();
	}

	/**
	 * 获取输入金额
	 *
	 * @return
	 */
	public BigDecimal getPromptTokensAmount() {
		return this.calculateChargeableDelegate.getPromptTokensAmount();
	}

	/**
	 * 计算
	 * @param calculateAmount
	 */
	public void calAmount(CalculateAmount calculateAmount) {
		if (StringUtils.isEmpty(this.calculateChargeableDelegate.getModelName())) {
			log.warn("task calAmount modelName is empty");
			log.info("calAmount{}", this);
			this.calculateChargeableDelegate.setCompletionTokensAmount(BigDecimal.ZERO);
			this.calculateChargeableDelegate.setPromptTokensAmount(BigDecimal.ZERO);
			return;
		}
		this.calculateChargeableDelegate.calAmount(calculateAmount);
	}
}
