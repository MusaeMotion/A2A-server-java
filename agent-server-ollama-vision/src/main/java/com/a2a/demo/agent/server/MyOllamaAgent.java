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

import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.agent.server.agent.*;
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import com.musaemotion.a2a.agent.server.utils.MediaUtils;
import com.musaemotion.a2a.common.constant.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

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
    public AgentGeneralResponse call(AgentRequest agentRequest) {
        // 过滤出文件对象
        List<Common.FilePart> fileParts = agentRequest.getParts().stream()
                .filter(item->item instanceof Common.FilePart)
                .map(item->(Common.FilePart)item).collect(Collectors.toUnmodifiableList());
        if(CollectionUtils.isEmpty(fileParts)){
           return AgentGeneralResponse.fromText("请上传图片", AgentResponseStatus.INPUT_REQUIRED);
        }
        List<Media> medias = MediaUtils.filePartToMedia(fileParts);

        // chatClient 用以下访问调用， chatModel是其他方式
        log.info("请求：{}", agentRequest.getText());
        String content = this.chatClient
                .prompt()
                .user(u-> u.text(AgentResponsePrompt.buildAgentResponsePrompt(agentRequest.getText())).media(medias.toArray(new Media[0])))
                // 可以使用自己的系统提示词
                .system(AgentResponsePrompt.buildAgentResponseSystem(a2aServerProperties.getDescription()+", 请用中文回复核心问题。"))
                .call().content();
        log.info("结果：{}", content);
        BeanOutputConverter<AgentTextResponse> converter = new BeanOutputConverter<>(AgentTextResponse.class);
        AgentTextResponse agentTextResponse = converter.convert(content);
        return AgentGeneralResponse.fromAgentTextResponse(agentTextResponse);
    }
}
