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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musaemotion.a2a.common.request.*;
import com.musaemotion.a2a.common.base.base.JSONRPCMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;



/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：终结点请求管理器
 */
@Component
@RequiredArgsConstructor
public class ServerEndpointManager {

  /**
   * 任务管理器
   */
  private final ITaskManager taskManager;

  /**
   * 消息处理
   *
   * @param request
   * @return
   */
  public JSONRPCMessage processRequest(LinkedHashMap request) {
    // 对象映射
    ObjectMapper objectMapper = new ObjectMapper();
    // 方法
    Object method = request.get("method");
    if (method.equals(GetTaskRequest.METHOD_PATH)) {
      return taskManager.onGetTask(objectMapper.convertValue(request, GetTaskRequest.class));
    }

    if (method.equals(CancelTaskRequest.METHOD_PATH)) {
      return taskManager.onCancelTask(objectMapper.convertValue(request, CancelTaskRequest.class));
    }

    if (method.equals(SendTaskRequest.METHOD_PATH)) {
      return taskManager.onSendTask(objectMapper.convertValue(request, SendTaskRequest.class));
    }

    if (method.equals(SetTaskPushNotificationRequest.METHOD_PATH)) {
      return taskManager.onSetTaskPushNotification(objectMapper.convertValue(request, SetTaskPushNotificationRequest.class));
    }

    if (method.equals(GetTaskPushNotificationRequest.METHOD_PATH)) {
      return taskManager.onGetTaskPushNotification(objectMapper.convertValue(request, GetTaskPushNotificationRequest.class));
    }
    throw new RuntimeException("Unexpected request type: " + request);
  }

  /**
   * 处理订阅消息
   * @param request
   * @return
   */
  public Flux<?> processRequestSubscribe(LinkedHashMap request) {
    ObjectMapper objectMapper = new ObjectMapper();
    Object method = request.get("method");
    if (method.equals(SendTaskStreamingRequest.METHOD_PATH)) {
      return taskManager.onSendTaskSubscribe(objectMapper.convertValue(request, SendTaskStreamingRequest.class));
    } else if (method.equals(TaskResubscriptionRequest.METHOD_PATH)) {
      return taskManager.onResubscribeToTask(objectMapper.convertValue(request, TaskResubscriptionRequest.class));
    }
    throw new RuntimeException("Unexpected request type: " + request);
  }
}
