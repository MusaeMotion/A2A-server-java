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

package com.a2a.demo.agent.sever;

import com.musaemotion.a2a.agent.server.manager.InMemoryTaskManager;
import com.musaemotion.a2a.agent.server.notification.PushNotificationSenderService;
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.sever
 * @project：A2A
 * @date：2025/5/12 12:52
 * @description：请完善描述
 */
public class MyTaskManager extends InMemoryTaskManager {

    /**
     * agent 实现该接口 AgentService
     * @param agent
     */
    public MyTaskManager(MyZhipuAgent agent, PushNotificationSenderService pushNotificationSenderService, A2aServerProperties serverProperties) {
        super(agent, pushNotificationSenderService, serverProperties);
    }
}
