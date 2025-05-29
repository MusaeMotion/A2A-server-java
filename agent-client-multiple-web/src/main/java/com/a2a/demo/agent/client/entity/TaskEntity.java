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

package com.a2a.demo.agent.client.entity;

import com.google.common.collect.Maps;
import com.vladmihalcea.hibernate.type.json.JsonType;
import com.musaemotion.a2a.common.base.Task;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.entity
 * @project：A2A
 * @date：2025/5/9 16:44
 * @description：请完善描述
 */
@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
public class TaskEntity {


	/**
	 * 主键id
	 */
	@Id
	private String id;

	/**
	 * 对话id
	 */
	private String conversationId;

	/**
	 * 产生任务的输入消息id
	 */
	private String inputMessageId;

	/**
	 * 消息id, 智能体之间传递的消息id
	 */
	private String messageId;

	/**
	 * 智能体 card 信息
	 */
	@Type(JsonType.class)
	@Column(length = 1000, columnDefinition = "json")
	private Task taskInfo;

	/**
	 * 创建时间
	 */
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;


	/**
	 * 构建对象
	 * @param id
	 * @param taskInfo
	 * @return
	 */
	public static TaskEntity newTaskEntity(String id, Task taskInfo){
		TaskEntity task = new TaskEntity();
		task.setId(id);
		task.setConversationId(taskInfo.getSessionId());
		task.setInputMessageId(taskInfo.getInputMessageId());
		task.setMessageId(taskInfo.getMessageId());
		task.setTaskInfo(taskInfo);
		task.setCreatedAt(LocalDateTime.now());
		return task;
	}

	/**
	 * 修改任务信息
	 * @param curTaskEntity
	 * @param source
	 * @return
	 */
	public static TaskEntity updateTaskInfo(TaskEntity curTaskEntity, Task source){
		Task target = curTaskEntity.getTaskInfo();
		target.setStatus(source.getStatus());
		if(target.getMetadata() == null){
			target.setMetadata(Maps.newConcurrentMap());
		}
		if(source.getMetadata() != null){
			target.getMetadata().putAll(source.getMetadata());
		}
		target.setHistory(source.getHistory());
		target.setArtifacts(source.getArtifacts());
		curTaskEntity.setTaskInfo(target);
		return curTaskEntity;
	}
	/**
	 * 获取Task
	 * @return
	 */
	public Task toTask(){
		return this.taskInfo;
	}
}
