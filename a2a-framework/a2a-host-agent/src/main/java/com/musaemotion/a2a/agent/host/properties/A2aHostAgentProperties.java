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

package com.musaemotion.a2a.agent.host.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;
/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.properties
 * @project：A2A
 * @date：2025/4/29 17:20
 * @description：请完善描述
 */
@Data
@ConfigurationProperties(prefix = "musaemotion.a2a.host-agent")
public class A2aHostAgentProperties {


    /**
     * 通知服务url地址，如果为空则不启动通知服务
     */
    private String notifyUrl;

    /**
     * 默认启动远程智能体地址列表
     */
    private List<String> remoteAgentAddresses;
}
