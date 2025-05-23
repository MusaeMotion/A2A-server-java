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

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.server.model
 * @project：A2A
 * @date：2025/4/22 15:33
 * @description：智能体响应提示词
 */

public class AgentResponsePrompt {

    /**
     * 用户提示词模板
     */
    private  final static String USER_INPUT_TEMPLATE = """ 
            {userInput}
            {format}
            """;

    /**
     * 系统模板
     */
    private final static String SYSTEM_TEMPLATE = """
            %s
            Set response status to input_required if the user needs to provide more information.
            Set response status to error if there is an error while processing the request.
            Set response status to completed if the request is complete.
            """;

    /**
     * 构建智能体输出格式的提示词
     * @param userInput 用户对话输入内容
     * @return 返回提示词文本
     */
    public static String buildAgentResponsePrompt(String userInput){
        BeanOutputConverter<AgentTextResponse> outputConverter = new BeanOutputConverter(AgentTextResponse.class);
		var promptTemplate = PromptTemplate.builder()
				.template(USER_INPUT_TEMPLATE)
				.variables(Map.of("userInput", userInput, "format", outputConverter.getFormat()))
				.build();
        return promptTemplate
                .createMessage()
                .getText();

    }


    /**
     * 系统提示词
     * @param description 当前智能体功能描述，生成系统提示词
     * @return
     */
    public static String buildAgentResponseSystem(String description){
        if(StringUtils.hasText(description)){
           return String.format(SYSTEM_TEMPLATE, description);
        }
        return String.format(SYSTEM_TEMPLATE, "");
    }
}
