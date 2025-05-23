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

import com.musaemotion.a2a.common.request.*;
import com.musaemotion.a2a.common.response.*;
import com.musaemotion.a2a.common.base.base.JSONRPCMessage;
import reactor.core.publisher.Flux;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：任务管理器接口
 */
public interface ITaskManager {

    /**
     * 获取任务
     * @param request
     * @return
     */
    GetTaskResponse onGetTask(GetTaskRequest request);

    /**
     * 取消任务
     * @param request
     * @return
     */
     CancelTaskResponse onCancelTask(CancelTaskRequest request);

    /**
     * 发送任务, 留给具体类去实现
     * @param request
     * @return
     */
    JSONRPCMessage onSendTask(SendTaskRequest request);

    /**
     * 发送任务流模式，留给具体实现方法去实现
     * @param request
     * @return
     */
    <T extends JSONRPCMessage> Flux<T> onSendTaskSubscribe(SendTaskStreamingRequest request);


    /**
     * 设置任务通知配置
     * @param request
     * @return
     */
     SetTaskPushNotificationResponse onSetTaskPushNotification(SetTaskPushNotificationRequest request);

    /**
     * 获取任务同送配置
     * @param request
     * @return
     */
     GetTaskPushNotificationResponse onGetTaskPushNotification(GetTaskPushNotificationRequest request);

    /**
     * 重新订阅
     * @param request
     * @return
     */
     Flux<SendTaskResponse> onResubscribeToTask(TaskResubscriptionRequest request);
}
