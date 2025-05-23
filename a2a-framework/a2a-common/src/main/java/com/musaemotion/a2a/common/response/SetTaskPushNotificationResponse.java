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

package com.musaemotion.a2a.common.response;

import com.musaemotion.a2a.common.response.result.TaskPushNotificationConfig;
import com.musaemotion.a2a.common.base.base.JSONRPCResponse;
import com.musaemotion.a2a.common.base.error.InternalA2aError;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class SetTaskPushNotificationResponse  extends JSONRPCResponse<TaskPushNotificationConfig> {


  /**
   * 构建错误
   * @param  requestId
   * @return
   */
  public static SetTaskPushNotificationResponse buildInternalError(String requestId) {
    SetTaskPushNotificationResponse response = new SetTaskPushNotificationResponse();
    return buildError(response, requestId, new InternalA2aError());
  }

  /**
   *
   * @param requestId
   * @param taskPushNotificationConfig
   * @return
   */
  public static SetTaskPushNotificationResponse buildResponse(String requestId, TaskPushNotificationConfig taskPushNotificationConfig) {
    SetTaskPushNotificationResponse response = new SetTaskPushNotificationResponse();
    response.setId(requestId);
    response.setResult(taskPushNotificationConfig);
    return response;
  }
}
