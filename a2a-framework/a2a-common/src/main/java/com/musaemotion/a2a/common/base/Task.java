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
import com.google.common.collect.Maps;
import com.musaemotion.a2a.common.IMetadata;
import com.musaemotion.a2a.common.constant.TaskState;
import com.musaemotion.a2a.common.event.AbstractTask;
import com.musaemotion.a2a.common.event.TaskArtifactUpdateEvent;
import com.musaemotion.a2a.common.event.TaskEvent;
import com.musaemotion.a2a.common.event.TaskStatusUpdateEvent;
import com.musaemotion.a2a.common.request.params.TaskSendParams;
import lombok.*;


import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
public class Task extends AbstractTask {


	// sessionId
	private String sessionId;

	// 返回的信息里面主要是先看状态字段
	private Common.TaskStatus status;

	// 返回的工件
	private List<Common.Artifact> artifacts;

	// 历史记录
	private List<Common.Message> history;


	/**
	 * 根据任务状态更新包装任务
	 * @param taskStatusUpdateEvent
	 * @return
	 */
	public static Task from(TaskStatusUpdateEvent taskStatusUpdateEvent){
		Task task = new Task();
		task.setId(taskStatusUpdateEvent.getId());
		task.setStatus(taskStatusUpdateEvent.getStatus());
		task.setMetadata(taskStatusUpdateEvent.getMetadata());
		return task;
	}

	/**
	 * 根据产出时间包装任务
	 * @param taskArtifactUpdateEvent
	 * @return
	 */
	public static Task from(TaskArtifactUpdateEvent taskArtifactUpdateEvent){
		Task task = new Task();
		task.setId(taskArtifactUpdateEvent.getId());
		task.setArtifacts(Lists.newArrayList(taskArtifactUpdateEvent.getArtifact()));
		task.setMetadata(taskArtifactUpdateEvent.getMetadata());
		return task;
	}

	/**
	 * 根据任务请求参数包装任务
	 * @param taskSendParams
	 * @return
	 */
	public static Task from(TaskSendParams taskSendParams, TaskState taskState){
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
	 * @param taskEvent
	 * @return
	 */
	public static Task buildSubmittedFrom(TaskEvent taskEvent){
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
	public Task copyNotification(){
		Task task = new Task();
		task.setId(this.getId());
		task.setSessionId(this.getSessionId());
		task.setMetadata(this.getMetadata());
		// 清空工件
		task.setArtifacts(Lists.newArrayList());
		// 历史记录清空
		task.setHistory(Lists.newArrayList());


		if(this.getStatus()!=null){
			var taskStatusBuilder = Common.TaskStatus.builder()
					.state(this.getStatus().getState())
					.timestamp(this.getStatus().getTimestamp());
			if(this.getStatus().getMessage()!=null){
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
	 * @return
	 */
	@JsonIgnore
	public String getInputMessageId(){
		if(metadata==null){
			return null;
		}
		if(!metadata.containsKey(INPUT_MESSAGE_ID)){
			return null;
		}
		return metadata.get(INPUT_MESSAGE_ID).toString();
	}

	/**
	 * 智能体交互消息id
	 * @return
	 */
	@JsonIgnore
	public String getMessageId(){
		if(metadata==null){
			return null;
		}
		if(!metadata.containsKey(MESSAGE_ID)){
			return null;
		}
		return metadata.get(MESSAGE_ID).toString();
	}


}
