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

package com.a2a.demo.agent.server;

import com.musaemotion.a2a.agent.server.agent.*;
import com.musaemotion.a2a.agent.server.service.PromptProvider;
import com.musaemotion.a2a.common.constant.MediaType;
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class MyAgent implements AgentService {

    private ChatClient chatClient;

    private A2aServerProperties a2aServerProperties;

	private PromptProvider promptProvider;
    /**
     *
     * @param chatModel
     * @param a2aServerProperties
     */
    @Autowired
    public MyAgent(ChatModel chatModel, A2aServerProperties a2aServerProperties, PromptProvider promptProvider){
       this.chatClient = ChatClient.create(chatModel);
       this.a2aServerProperties = a2aServerProperties;
	   this.promptProvider = promptProvider;
    }

    /**
     *
     * @return
     */
    @Override
    public List<MediaType> supportedContentTypes() {
        return List.of(MediaType.TEXT);
    }

    /**
     * agent名称
     * @return
     */
    @Override
    public String agentName() {
        return a2aServerProperties.getName();
    }

    /**
     * 流请求
     * @param agentRequest
     * @return
     */
    @Override
    public Flux<AgentGeneralResponse> stream(AgentRequest agentRequest) {
        return this.chatClient
                .prompt(AgentResponsePrompt.buildAgentResponsePrompt(agentRequest.getText()))
                // 默认用的配置文件的描述作为的系统提示词
                // .system(AgentResponsePrompt.buildAgentResponseSystem(a2aServerProperties.getDescription()))
				.system(AgentResponsePrompt.buildAgentResponseSystem(this.promptProvider.getPrompt()))
                .stream()
                .chatResponse()
                .doOnComplete(()->{})
                .map(chatResponse -> {
                    String content = chatResponse.getResult().getOutput().getText();
                    return AgentGeneralResponse.fromText(content, AgentResponseStatus.WORKING);
                })
				.concatWith(Mono.just(AgentGeneralResponse.fromText("", AgentResponseStatus.COMPLETED)));
    }

    /**
     *
     * @param agentRequest
     * @return
     */
    @Override
    public AgentGeneralResponse call(AgentRequest agentRequest) {
        String content = this.chatClient
                // 这里需要严格按照这个方式创建提示词
                .prompt(AgentResponsePrompt.buildAgentResponsePrompt(agentRequest.getText()))
                // 可以使用自己的系统提示词
				.system(AgentResponsePrompt.buildAgentResponseSystem(this.promptProvider.getPrompt()))
                .call().content();
        BeanOutputConverter<AgentTextResponse> converter = new BeanOutputConverter<>(AgentTextResponse.class);
        AgentTextResponse agentTextResponse =  converter.convert(content);
        return AgentGeneralResponse.fromAgentTextResponse(agentTextResponse);
    }
}
