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

package com.musaemotion.a2a.agent.server.agent;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.server.model
 * @project：A2A
 * @date：2025/4/22 15:27
 * @description：智能体结构化输出
 */
@Data
@NoArgsConstructor
public class AgentTextResponse implements Serializable {
   /**
    * 状态
    */
   private AgentResponseStatus status;

   /**
    * 智能体返回内容
    */
   private String content;
}
