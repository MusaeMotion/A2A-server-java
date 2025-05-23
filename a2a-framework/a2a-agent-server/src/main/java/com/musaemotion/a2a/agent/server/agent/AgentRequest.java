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

import com.musaemotion.a2a.common.base.Common;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.server.model
 * @project：A2A
 * @date：2025/4/22 10:33
 * @description：请完善描述
 */
@Data
@Builder
public class AgentRequest {
    /**
     * 文本消息
     */
    private String text;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 多模态文件列表
     */
    private List<Common.Part> parts;
}
