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

package com.musaemotion.a2a.agent.host.manager;

import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.util.List;
/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.service
 * @project：A2A
 * @date：2025/5/9 15:04
 * @description：请完善描述
 */
public abstract class AbstractTaskCenterManager {

	/**
	 * 智能体相互调用过程中，消息id关系，后面选择落库。
	 */
	@Getter
	private Map<String, String> nextId = new HashMap<String, String>();

	/**
	 * 处理工件使用的map
	 */
	@Getter
	private Map<String, Map<Integer, Common.Artifact>> artifactChunks = new HashMap<>();

	/**
	 * 添加任务
	 * @param task
	 */
	public abstract Task addTask(Task task);

	/**
	 * 是否存在
	 * @param taskId
	 * @return
	 */
	public abstract Boolean exists(String taskId);

	/**
	 * 根据消息ids列表获取任务列表
	 * @param inputMessageIds
	 * @return
	 */
	public abstract List<Task> listByInputMessageId(List<String> inputMessageIds);

	/**
	 * 根据消息id获取对应的任务
	 * @param messageId
	 * @return
	 */
	public abstract Optional<Task> getByMessageId(String messageId);

	/**
	 * 根据任务id获取任务
	 * @param taskId
	 * @return
	 */
	public abstract Optional<Task> getById(String taskId);

	/**
	 * 根据对话id获取列表
	 * @param conversationId
	 * @return
	 */
	public abstract List<Task> getByConversationId(String conversationId);

	/**
	 * 根据对话id删除
	 * @param conversationId
	 */
	public abstract void deleteByConversationId(String conversationId);

	/**
	 * 删除任务
	 * @param taskIds
	 */
	public abstract void removeTask(List<String> taskIds) ;

	/**
	 * 更新任务
	 * @param task
	 */
	public abstract void updateTask(Task task);
}
