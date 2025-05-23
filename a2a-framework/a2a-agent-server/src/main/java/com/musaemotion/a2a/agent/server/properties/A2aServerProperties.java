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

package com.musaemotion.a2a.agent.server.properties;

import com.google.common.collect.Lists;
import com.musaemotion.a2a.common.AgentCard;
import com.musaemotion.a2a.common.AgentSkill;
import com.musaemotion.a2a.common.constant.MediaType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description: a2a 配置
 */
@Data
@ConfigurationProperties(prefix = "musaemotion.a2a.server")
public class A2aServerProperties {

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * url地址, 暴露给 智能体发现服务访问的地址
     * 例如 http://127.0.0.1:8888/
     */
    private String url;

    /**
     * 版本
     */
    private String version;

    /**
     * 文档url
     */
    private String documentationUrl;

    /**
     * agent 提供者
     */
    private AgentCard.AgentProvider provider;

    /**
     * agent 能力
     */
    private AgentCard.AgentCapabilities capabilities;

    /**
     * agent 能力
     */
    private AgentCard.AgentAuthentication authentication;

    /**
     * 默认输入类型
     */
    private List<MediaType> defaultInputModes = Lists.newArrayList(MediaType.TEXT);

    /**
     * 默认输出类型
     */
    private List<MediaType> defaultOutputModes = Lists.newArrayList(MediaType.TEXT);

    /**
     * 支持的能力
     */
    private List<AgentSkill> skills;

    /**
     * agent 提供者信息
     * @param organization
     * @param url
     */
    public record AgentProvider(String organization, String url) {}

    /**
     * agent 能力
     * @param streaming
     * @param pushNotifications
     * @param stateTransitionHistory
     */
    public record AgentCapabilities(boolean streaming, boolean pushNotifications, boolean stateTransitionHistory) { }

    /**
     * agent 授权相关
     * @param schemes
     * @param credentials
     */
    public record AgentAuthentication(List<String> schemes, String credentials) {}
}
