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

package com.musaemotion.a2a.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.constant.MediaType;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：智能体Card
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentCard implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * url地址
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
    private AgentProvider provider;

    /**
     * agent 能力
     */
    private AgentCapabilities capabilities;

    /**
     * agent 该智能体的授权模式
     */
    private Common.AgentAuthentication authentication;

    /**
     * 默认输入类型
     */
    @Builder.Default
    private List<MediaType> defaultInputModes = Lists.newArrayList(MediaType.TEXT);

    /**
     * 默认输出类型
     */
    @Builder.Default
    private List<MediaType> defaultOutputModes = Lists.newArrayList(MediaType.TEXT);

    /**
     * 支持的技能
     */
    @Builder.Default
    private List<AgentSkill> skills = Lists.newArrayList();

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
	 * @param modifyPrompt (非标准a2a原协议内容)
     */
    public record AgentCapabilities(boolean streaming, boolean pushNotifications, boolean stateTransitionHistory, boolean modifyPrompt) { }


	/**
	 * to Json
	 * @return
	 * @throws JsonProcessingException
	 */
	public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(this);
    }

}
