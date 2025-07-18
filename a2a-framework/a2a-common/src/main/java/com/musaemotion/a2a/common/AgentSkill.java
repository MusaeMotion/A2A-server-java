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

import com.google.common.collect.Lists;
import com.musaemotion.a2a.common.constant.MediaType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：支持的能力
 */
@Data
public class AgentSkill {

    /**
     * skillId 和 agentCard 的 name 不要重复，会影响 大模型推理的注意力，从而产生工具调用幻觉
     */
    private String id;

    /**
     * skillName 和 agentCard 的 name 不要重复，会影响 大模型推理的注意力，从而产生工具调用幻觉
     */
    private String name;

    /**
     * 描述
     */
    private String description;
    /**
     * tags
     */
    private List<String> tags = new ArrayList<>();

    /**
     * 示例
     */
    private List<String> examples;

    /**
     * 输入的模式
     */
    private List<MediaType> inputModes = Lists.newArrayList(MediaType.TEXT);

    /**
     * 输出的模式
     */
    private List<MediaType> outputModes= Lists.newArrayList(MediaType.TEXT);
}
