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

package com.a2a.demo.agent.client.service;

import com.a2a.demo.agent.client.entity.TaskEntity;
import com.a2a.demo.agent.client.repository.TaskRepository;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.agent.host.manager.AbstractTaskCenterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.service
 * @project：A2A
 * @date：2025/5/9 16:43
 * @description：请完善描述
 */
@Service
@Slf4j
public class MysqlTaskCenterManager extends AbstractTaskCenterManager {

	private TaskRepository repository;

	@Autowired
	public MysqlTaskCenterManager(TaskRepository repository){
		this.repository = repository;
	}

	/**
	 * 创建任务落库
	 * @param task
	 * @return
	 */
	@Override
	public Task addTask(Task task) {
		var op = this.repository.findById(task.getId());
		if (op.isPresent()) {
			log.warn("该任务已经存在");
			return op.get().toTask();
		}
		TaskEntity taskEntity = TaskEntity.newTaskEntity(task.getId(), task);
		this.repository.save(taskEntity);
		return taskEntity.toTask();
	}

	/**
	 * 判断任务ID是否存在
	 * @param taskId
	 * @return
	 */
	@Override
	public Boolean exists(String taskId) {
		return this.repository.existsById(taskId);
	}

	/**
	 * 根据用户输入的消息id获取相关的任务
	 * @param inputMessageIds
	 * @return
	 */
	@Override
	public List<Task> listByInputMessageId(List<String> inputMessageIds) {
		return this.repository.findAllByInputMessageIdIn(inputMessageIds).stream().map(item->item.toTask()).collect(Collectors.toList());
	}

	/**
	 * 根据消息id获取相关任务
	 * @param messageId
	 * @return
	 */
	@Override
	public Optional<Task> getByMessageId(String messageId) {
		var optional = this.repository.findByMessageId(messageId);
		if(optional.isPresent()) {
			return Optional.of(optional.get().toTask());
		}
		return Optional.empty();
	}

	/**
	 * 根据任务id获取相关任务
	 * @param taskId
	 * @return
	 */
	@Override
	public Optional<Task> getById(String taskId) {
		var optional = this.repository.findById(taskId);
		if(optional.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(optional.get().toTask());
	}

	/**
	 * 根据交谈id获取相关的任务，如果交谈id为空，则返回所有任务
	 * @param conversationId
	 * @return
	 */
	@Override
	public List<Task> getByConversationId(String conversationId) {

		if(StringUtils.hasText(conversationId)){
			return this.repository.findByConversationIdOrderByCreatedAtDesc(conversationId).stream().map(item->item.toTask()).collect(Collectors.toList());
		}
		return this.repository.findAllByOrderByCreatedAtDesc().stream().map(item->item.toTask()).collect(Collectors.toList());
	}

	/**
	 * 根据交谈id删除任务
	 * @param conversationId
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteByConversationId(String conversationId) {
		this.repository.deleteByConversationId(conversationId);
	}

	/**
	 * 根据任务id删除任务
	 * @param taskIds
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeTask(List<String> taskIds) {
		taskIds.forEach(taskId -> {
			this.repository.deleteById(taskId);
		});
	}

	/**
	 * 更新插入任务
	 * @param task
	 */
	@Override
	public void updateTask(Task task) {
		var op = this.repository.findById(task.getId());
		if (op.isEmpty()) {
			log.warn("该任务不存在");
			return;
		}
		TaskEntity taskEntity = op.get();
		taskEntity = TaskEntity.updateTaskInfo(taskEntity, task);
		this.repository.save(taskEntity);
	}
}
