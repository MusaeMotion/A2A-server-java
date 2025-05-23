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

package com.musaemotion.a2a.common.response.result;

import com.musaemotion.a2a.common.base.Common;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：任务发布通知配置
 */
@Data
@Builder
public class TaskPushNotificationConfig implements Serializable {

  /**
   * id
   */
  private String id;

  /**
   * 发布消息配置
   */
  private Common.PushNotificationConfig pushNotificationConfig;


}
