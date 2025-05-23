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

package com.musaemotion.a2a.common.base.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：
 */
@Data
public class JSONRPCMessage implements Serializable {

  /**
   * json标准
   */
  @JsonProperty("jsonrpc")
  private String jsonrpc = "2.0";

  /**
   * 请求 id, 每次请求都需要重新创建，每次请求唯一
   */
  private String id;
}
