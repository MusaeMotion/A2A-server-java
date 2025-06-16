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

package com.musaemotion.a2a.agent.server.manager;

import com.musaemotion.a2a.agent.server.agent.AgentService;
import com.musaemotion.a2a.agent.server.entity.PushNotificationEntity;
import com.musaemotion.a2a.agent.server.entity.TaskEntity;
import com.musaemotion.a2a.agent.server.notification.PushNotificationSenderService;
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import com.musaemotion.a2a.agent.server.repository.PushNotificationRepository;
import com.musaemotion.a2a.agent.server.repository.TaskRepository;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：内存任务管理器
 */
@Slf4j
@Service
public class MysqlJpaTaskManager extends AbstractTaskManager  {

	private final TaskRepository taskRepository;

	private final PushNotificationRepository pushNotificationRepository;

	@Autowired
	public MysqlJpaTaskManager(
			PushNotificationSenderService pushNotificationSenderService,
			AgentService agentService,
			A2aServerProperties a2aServerProperties,
			TaskRepository taskRepository,
			PushNotificationRepository pushNotificationRepository
	) {
		super(pushNotificationSenderService, agentService, a2aServerProperties);
		this.taskRepository = taskRepository;
		this.pushNotificationRepository = pushNotificationRepository;
	}

	/**
	 * 推送设置
	 * @param taskId
	 * @param notificationConfig
	 * @return
	 */
	@Transactional
	public Boolean setPushNotificationInfoToStore(String taskId, Common.PushNotificationConfig notificationConfig) {
		Optional<TaskEntity> taskOptional = this.taskRepository.findById(taskId);
		if (!taskOptional.isPresent()) {
			log.info("taskId:{} not exist", taskId);
			return Boolean.FALSE;
		}
		PushNotificationEntity config = new PushNotificationEntity();
		config.setTaskId(taskId);
		config.setUrl(notificationConfig.getUrl());
		config.setAuthentication(notificationConfig.getAuthentication());
		config.setToken(notificationConfig.getToken());
		this.pushNotificationRepository.save(config);
		return Boolean.TRUE;
	}

	/**
	 * 获取task任务对应的推送消息设置
	 * @param taskId
	 * @return
	 */
	@Transactional(readOnly = true)
	public Optional<Common.PushNotificationConfig> getPushNotificationInfoForStore(String taskId) {
		Optional<TaskEntity> taskOptional = this.taskRepository.findById(taskId);
		if (taskOptional.isEmpty()) {
			return Optional.empty();
		}
		Optional<PushNotificationEntity>  optional = pushNotificationRepository.findByTaskId(taskId);
		if (optional.isPresent()) {
			return Optional.of(
					optional.get().to()
			);
		}
		return Optional.empty();
	}

	/**
	 * 任务落库操作
	 * @param taskId
	 * @param task
	 */
	@Transactional
	public void setTaskToStore(String taskId, Task task) {
		TaskEntity taskEntity = TaskEntity.from(task);
		taskEntity.setId(taskId);
		taskRepository.save(taskEntity);
	}

	/**
	 * 获取存储的任务信息
	 * @param taskId
	 * @return
	 */
	@Transactional(readOnly = true)
	public Optional<Task> getTaskForStore(String taskId) {
		var taskOptional = taskRepository.findById(taskId);
		if (taskOptional.isEmpty()) {
			return Optional.empty();
		}
		TaskEntity taskEntity = taskOptional.get();
		return Optional.of(taskEntity.toTask());
	}

}
