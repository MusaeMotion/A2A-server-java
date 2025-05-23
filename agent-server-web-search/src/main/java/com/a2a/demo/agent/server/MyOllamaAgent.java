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

import com.a2a.demo.agent.server.page.ItemDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.agent.server.agent.*;
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.constant.MediaType;
import com.musaemotion.a2a.common.utils.GuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class MyOllamaAgent implements AgentService {

    private ChatClient chatClient;

    private A2aServerProperties a2aServerProperties;

    /**
     * @param chatModel
     * @param a2aServerProperties
     */
    @Autowired
    public MyOllamaAgent(OllamaChatModel chatModel, A2aServerProperties a2aServerProperties){
       this.chatClient = ChatClient.create(chatModel);
       this.a2aServerProperties = a2aServerProperties;
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
        throw new NotImplementedException("该智能体未实现");
    }

    /**
     *
     * @param agentRequest
     * @return
     */
    @Override
    public AgentGeneralResponse call(AgentRequest agentRequest) throws JsonProcessingException {
        log.info("请求：{}", agentRequest.getText());
       WebTools	webTools = new WebTools();
        String content = this.chatClient
                .prompt()
                .user(u-> u.text(AgentResponsePrompt.buildAgentResponsePrompt(agentRequest.getText())))
				.user(u-> u.text(agentRequest.getText()))
				.tools(webTools)
                .system(AgentResponsePrompt.buildAgentResponseSystem(a2aServerProperties.getDescription()+", 请用中文回复核心问题。"))
                .call().content();

		ObjectMapper objectMapper = new ObjectMapper();
		List<Common.Part> parts = Lists.newArrayList();

		ItemDto[] news = objectMapper.readValue(content, ItemDto[].class);
		AgentGeneralResponse agentGeneralResponse = new AgentGeneralResponse();
		agentGeneralResponse.setStatus(AgentResponseStatus.COMPLETED);
		Arrays.stream(news).toList().forEach(itemDto -> {
			if(webTools.getProgram().equals("图片")){
				var fileContent = new Common.FileContent();
				Common.FilePart part =  Common.FilePart.newFilePart(fileContent);
				fileContent.setMimeType(MediaType.IMAGE_PNG.getValue());
				fileContent.setUri(itemDto.getContent());
				fileContent.setName(GuidUtils.createShortRandomGuid());
				parts.add(part);
				return;
			}
			Common.TextPart part = new Common.TextPart();
			part.setText(itemDto.getContent()+", 文献地址："+itemDto.getUrl());
			parts.add(part);
		});
		agentGeneralResponse.setParts(parts);

        return agentGeneralResponse;
    }
}
