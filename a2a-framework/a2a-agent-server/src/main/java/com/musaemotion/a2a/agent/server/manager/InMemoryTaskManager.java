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

import com.google.common.collect.Maps;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.agent.server.notification.PushNotificationSenderService;
import com.musaemotion.a2a.agent.server.agent.AgentService;
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：内存任务管理器
 */
@Slf4j
public  class InMemoryTaskManager extends AbstractTaskManager  {

    /**
     * 任务信息
     */
    protected Map<String, Task> tasks;

    /**
     * 推送配置信息
     */
    private Map<String, Common.PushNotificationConfig> pushNotificationInfos;

    /**
     * 内容任务管理
     */
    public InMemoryTaskManager(AgentService agentService, PushNotificationSenderService pushNotificationSenderService, A2aServerProperties a2aServerProperties) {
        super(pushNotificationSenderService, agentService, a2aServerProperties);
        this.tasks = Maps.newConcurrentMap();
        this.pushNotificationInfos = Maps.newConcurrentMap();
    }

    /**
     * 推送设置落库
     * @param taskId
     * @param notificationConfig
     */
    public Boolean setPushNotificationInfoToStore(String taskId, Common.PushNotificationConfig notificationConfig) {
        if (!this.tasks.containsKey(taskId)) {
            log.error("taskId:{} not exist", taskId);
            return Boolean.FALSE;
        }
        this.pushNotificationInfos.put(taskId, notificationConfig);
        return Boolean.TRUE;
    }

    /**
     * 获取task任务对应的推送消息设置
     * @param taskId
     * @return
     */
    public Optional<Common.PushNotificationConfig> getPushNotificationInfoForStore(String taskId) {
        if (!this.tasks.containsKey(taskId)) {
            return Optional.empty();
        }
        var config = this.pushNotificationInfos.get(taskId);
        if (config == null) {
            return Optional.empty();
        }
        return Optional.of(
                config
        );
    }

    /**
     * 任务落库操作
     * @param taskId
     * @param task
     */
    public void setTaskToStore(String taskId, Task task) {
        this.tasks.put(taskId, task);
    }


    /**
     * 获取存储的任务信息
     * @param taskId
     * @return
     */
    public Optional<Task> getTaskForStore(String taskId) {
        Task task = this.tasks.get(taskId);
        if (task == null) {
            return Optional.empty();
        }
        return Optional.of(task);
    }

}
