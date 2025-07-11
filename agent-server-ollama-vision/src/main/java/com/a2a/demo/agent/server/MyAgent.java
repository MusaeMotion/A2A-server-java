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
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import com.musaemotion.a2a.agent.server.utils.MediaUtils;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.constant.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MyAgent implements AgentService {

    private ChatClient chatClient;

    private A2aServerProperties a2aServerProperties;

    /**
     * @param chatModel
     * @param a2aServerProperties
     */
    @Autowired
    public MyAgent(ChatModel chatModel, A2aServerProperties a2aServerProperties){
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

	@Override
	public String useModel() {
		return "qwen-vl-max-latest";
	}

	/**
     * 流请求
     * @param agentRequest
     * @return
     */
    @Override
    public Flux<AgentGeneralResponse> stream(AgentRequest agentRequest) {

		// 过滤出文件对象
		List<Common.FilePart> fileParts = agentRequest.getParts().stream()
				.filter(item->item instanceof Common.FilePart)
				.map(item->(Common.FilePart)item).collect(Collectors.toUnmodifiableList());

		List<Media> medias = MediaUtils.filePartToMedia(fileParts);

		return this.chatClient
				.prompt()
				.user(u-> u.text(AgentResponsePrompt.buildAgentResponsePrompt(agentRequest.getText())).media(medias.toArray(new Media[0])))
				.system(AgentResponsePrompt.buildAgentResponseSystem(this.a2aServerProperties.getDescription()+", 请用中文回复核心问题。"))
				.stream()
				.chatResponse()
				.doOnComplete(()->{})
				.map(chatResponse -> {
					return AgentGeneralResponse.fromStreamChatResponse(chatResponse, AgentResponseStatus.WORKING);
				});
    }

    /**
     *
     * @param agentRequest
     * @return
     */
    @Override
    public AgentGeneralResponse call(AgentRequest agentRequest) {
        // 过滤出文件对象
        List<Common.FilePart> fileParts = agentRequest.getParts().stream()
                .filter(item->item instanceof Common.FilePart)
                .map(item->(Common.FilePart)item).collect(Collectors.toUnmodifiableList());
        if(CollectionUtils.isEmpty(fileParts)){
           return AgentGeneralResponse.fromText("请上传图片", AgentResponseStatus.INPUT_REQUIRED);
        }
        List<Media> medias = MediaUtils.filePartToMedia(fileParts);

		ChatResponse chatResponse = this.chatClient
                .prompt()
                .user(u-> u.text(AgentResponsePrompt.buildAgentResponsePrompt(agentRequest.getText())).media(medias.toArray(new Media[0])))
                // 可以使用自己的系统提示词
                .system(AgentResponsePrompt.buildAgentResponseSystem(this.a2aServerProperties.getDescription()+", 请用中文回复核心问题。"))
                .call().chatResponse();
        return AgentGeneralResponse.fromCallChatResponse(chatResponse);
    }
}
