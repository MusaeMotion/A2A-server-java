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

package com.musaemotion.a2a.agent.host.constant;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.configuration
 * @project：A2A
 * @date：2025/4/30 16:09
 * @description：请完善描述
 */
public interface ControllerSetting {

    /**
     * 根
     */
    String ROOT_URL = "/api";

    /**
     * 聊天
     */
    String CHAT = ROOT_URL + "/chat";

    /**
     * 任务
     */
    String TASK = ROOT_URL + "/task";


    /**
     * 远程智能体
     */
    String REMOTE_AGENT = ROOT_URL + "/remote-agent";

    /**
     * 对话
     */
    String CONVERSATION = ROOT_URL + "/conversation";
}
