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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.musaemotion.a2a.common.constant.MediaType;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.server.service
 * @project：A2A
 * @date：2025/4/22 10:31
 * @description：请完善描述
 */
public interface AgentService {

    /**
     * 智能体支持的类型
     * @return
     */
    List<MediaType> supportedContentTypes();

    /**
     * agent名称，用于消息推送区分jwk使用
     * @return
     */
    String agentName();

    /**
     * 流模式
     * @param agentRequest
     * @return
     */
    Flux<AgentGeneralResponse> stream(AgentRequest agentRequest) throws NoSuchMethodException;

    /**
     * 同步请求模式
     * @param agentRequest
     * @return
     */
    AgentGeneralResponse call(AgentRequest agentRequest) throws JsonProcessingException;
}
