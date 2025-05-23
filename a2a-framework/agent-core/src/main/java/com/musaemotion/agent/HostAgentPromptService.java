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

package com.musaemotion.agent;

import java.util.Map;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.framework
 * @project：A2A
 * @date：2025/4/28 13:26
 * @description：主机智能体提示词
 */
public interface HostAgentPromptService {

    /**
     * 获取
     * @param state
     * @return
     */
   String hostAgentSystemPrompt(Map<String, Object> state);

    /**
     * 远程智能体列表
     * @return
     */
   String loadRemoteAgentsToString();
}
