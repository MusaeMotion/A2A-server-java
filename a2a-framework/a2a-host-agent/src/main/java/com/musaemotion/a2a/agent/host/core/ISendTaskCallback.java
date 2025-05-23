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

package com.musaemotion.a2a.agent.host.core;

import com.musaemotion.a2a.common.event.TaskArtifactUpdateEvent;
import com.musaemotion.a2a.common.event.TaskStatusUpdateEvent;
import com.musaemotion.a2a.common.base.Task;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.client
 * @project：A2A
 * @date：2025/4/27 22:53
 * @description：HostAgent: fun(sendTask)-> obj(client: A2aRemoteAgentConnections).sendTask() 调用的回调
 */
public interface ISendTaskCallback {

    /**
     * 提交
     * @param task
     */
    void sendTaskCallback(Task task);

    /**
     * 任务状态更新
     * @param taskStatusUpdateEvent
     */
    void sendTaskCallback(TaskStatusUpdateEvent taskStatusUpdateEvent);

    /**
     * 任务工件更新
     * @param taskArtifactUpdateEvent
     */
    void sendTaskCallback(TaskArtifactUpdateEvent taskArtifactUpdateEvent);
}
