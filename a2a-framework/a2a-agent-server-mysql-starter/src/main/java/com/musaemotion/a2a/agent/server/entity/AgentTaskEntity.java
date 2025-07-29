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

package com.musaemotion.a2a.agent.server.entity;

import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.common.constant.TaskState;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.entity
 * @project：A2A
 * @date：2025/5/9 16:44
 * @description：请完善描述
 */
@Entity
@Table(name = "agent_task")
@Data
@NoArgsConstructor
public class AgentTaskEntity {

	@Id
	@Column(name = "id", length = 36)
	private String id;

	@Column(name = "session_id", length = 36)
	private String sessionId;

	@Column(name = "input_message_id", length = 36)
	private String inputMessageId;

	@Column(name = "message_id", length = 36)
	private String messageId;



	/**
	 * 任务消息
	 */
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private Common.Message statusMessage;

	/**
	 * 时间戳
	 */
	private String statusTimestamp;

	/**
	 * 任务状态
	 */
	@Enumerated(EnumType.STRING)
	private TaskState statusState;

	/**
	 * 生成的工件
	 */
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private List<Common.Artifact> artifacts;

	/**
	 * 聊天记录
	 */
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private List<Common.Message> history;

	/**
	 * 其他数据
	 */
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private Map<String,Object> metadata;

	/**
	 * 构建一个task实体
	 * @param task
	 * @return
	 */
   public static AgentTaskEntity from(Task task) {
	   AgentTaskEntity agentTaskEntity = new AgentTaskEntity();
	   agentTaskEntity.setId(task.getId());
	   agentTaskEntity.setSessionId(task.getSessionId());
	   if (task.getInputMessageId() != null) {
		   agentTaskEntity.setInputMessageId(task.getInputMessageId());
	   }
	   if (task.getMessageId() != null) {
		   agentTaskEntity.setMessageId(task.getMessageId());
	   }

	   if (task.getHistory() != null) {
		   agentTaskEntity.setHistory(task.getHistory());
	   }

	   if (task.getArtifacts() != null) {
		   agentTaskEntity.setArtifacts(task.getArtifacts());
	   }
	   if (task.getMetadata() != null) {
		   agentTaskEntity.setMetadata(task.getMetadata());
	   }

	   agentTaskEntity.setStatusState(task.getStatus().getState());
	   agentTaskEntity.setStatusTimestamp(task.getStatus().getTimestamp());
	   if (task.getStatus().getMessage() != null) {
		   agentTaskEntity.setStatusMessage(task.getStatus().getMessage());
	   }
	   return agentTaskEntity;
   }

	/**
	 * toTask
	 * @return
	 */
	public Task toTask() {
	   Task task = new Task();
	   BeanUtils.copyProperties(this, task);
	   return task;
   }
}
