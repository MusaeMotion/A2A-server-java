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

package com.musaemotion.agent;

import com.google.common.collect.Lists;
import com.musaemotion.a2a.common.utils.GuidUtils;
import com.musaemotion.a2a.common.utils.PartUtils;
import com.musaemotion.agent.model.FileInfo;
import com.musaemotion.agent.model.ModelHyperParams;
import com.musaemotion.agent.model.SendMessageRequest;
import io.micrometer.observation.ObservationRegistry;
import lombok.Builder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.content.Media;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.framework
 * @project：A2A
 * @date：2025/4/28 13:09
 * @description：请完善描述
 */
@Builder
@Slf4j
public class BasisAgent<T extends SendMessageRequest> {

	/**
	 * 开始请求时间
	 */
	public static final String BEGIN_TIME = "beginTime";

    /**
     * 状态值，扩展
     */
    public static final String STATE = "state";

    /**
     * 智能体id
     */
    private String id;

    /**
     * 智能体名称
     */
    private String name;

    /**
     * 智能体描述
     */
    private String description;

    /**
     * ChatClient
     */
	@Setter
    private ChatClient chatClient;

    /**
     * 可观察性
     */
    private ObservationRegistry observationRegistry;

    /**
     * 超参数
     */
    @Builder.Default
    private ModelHyperParams modelHyperParams = ModelHyperParams.builder().build();

    /**
     * 聊天记录读取行数
     */
    @Builder.Default
    private Integer chatMemorySize = 10;


    /**
     * 提示词内存存储方案
      */
    private ChatMemoryRepository chatMemoryRepository;

    /**
     * 系统提示词请在该接口实现
     */
    private AgentPromptProvider agentPromptProvider;


    /**
     * 工具列表
     */
    @Builder.Default
    private List<ToolCallback> toolCallbacks = Lists.newArrayList();


	/**
	 * 构造user 提示词内容
	 * @param input
	 * @return
	 */
	private String buildUserPrompt(T input) {
		StringBuffer stringBuffer = new StringBuffer(agentPromptProvider.userPrompt(input.getMetadata()));
		stringBuffer.append("\n");
		stringBuffer.append(PartUtils.getTextContent(input.getParams()));
		return stringBuffer.toString();
	}


    /**
     * 处理参数，和userText
     * @param userText
     * @return
     */
    protected ChatClient.ChatClientRequestSpec buildChatClientParams(String userText, List<FileInfo> files){
        List<Media> medias;
        if(!CollectionUtils.isEmpty(files)){
            medias = files.stream().map(item-> {
                return Media.builder()
                        .mimeType(MimeType.valueOf(item.getMime()))
                        .name(item.getFileName())
                        .data(item.getResource())
                        .build();
            }).collect(Collectors.toList());

        } else {
            medias = Lists.newArrayList();
        }
        var prompt = this.chatClient.prompt();

        var chat = prompt.user((promptUserSpec) -> {
            promptUserSpec.text(userText);
            if(!CollectionUtils.isEmpty(medias)){
                promptUserSpec.media(medias.toArray(new Media[0]));
            }
        });


		return chat;
    }


	/**
	 * 构建日志顾问
	 * @return
	 */
    private SimpleLoggerAdvisor loggerAdvisor(){
        return new SimpleLoggerAdvisor();
    }

	/**
	 * 构建顾问
	 * @param input
	 * @return
	 */
    public List<Advisor> buildAdvisor(T input) {
        List<Advisor> advisors = Lists.newArrayList();
        advisors.add(loggerAdvisor());

		var memoryBuilder = MessageWindowChatMemory.builder()
				.chatMemoryRepository(this.chatMemoryRepository);
		if (this.chatMemorySize != null && this.chatMemorySize>0) {
			memoryBuilder.maxMessages(chatMemorySize);
		}
		advisors.add(MessageChatMemoryAdvisor
				.builder(memoryBuilder.build())
				.conversationId(input.getConversationId())
				.build()
		);

        return advisors;
    }


	/**
	 * 同步调用
	 * @param input 输入内容
	 * @param toolContext 工具上下文
	 * @param files 附件内容
	 * @return
	 */
	public AssistantMessage call(T input, Map<String, Object> toolContext, List<FileInfo> files) {
        ChatClient.ChatClientRequestSpec chatClientRequestSpec = buildChatClientParams(input.getContent(), files);
		chatClientRequestSpec = chatClientRequestSpec.system(agentPromptProvider.systemPrompt(toolContext, input.getMetadata()));
        ChatResponse chatResponse = chatClientRequestSpec.advisors(buildAdvisor(input))
                .user(buildUserPrompt(input))
				.toolCallbacks(this.toolCallbacks)
                .toolContext(toolContext)
                .call()
                .chatResponse();

        return chatResponse.getResult().getOutput();
    }


	/**
	 * stream 调用
	 * @param input 输入内容
	 * @param toolContext 工具上下文
	 * @param files 附件内容
	 * @return
	 */
	public Flux<AssistantMessage> stream(T input, Map<String, Object> toolContext, List<FileInfo> files) {
		ChatClient.ChatClientRequestSpec chatClientRequestSpec = buildChatClientParams(buildUserPrompt(input), files);
		chatClientRequestSpec = chatClientRequestSpec.system(agentPromptProvider.systemPrompt(toolContext, input.getMetadata()));
		return chatClientRequestSpec
				.advisors(buildAdvisor(input))
				.toolCallbacks(this.toolCallbacks)
				.toolContext(toolContext)
				.stream()
				.chatResponse()
				.doOnComplete(() ->  log.info("BasisAgent 智能体stream响应完成"))
				.doOnError(s -> Boolean.TRUE, s -> log.error("BasisAgent 智能体执行出现了异常"))
				.onErrorResume((error) -> {
					var generation = new Generation(
							new AssistantMessage("人工智能出现了一点问题, 稍后再试" ),
							ChatGenerationMetadata.builder().finishReason(GuidUtils.createGuid()).build()
					);
					log.error("BasisAgent error: {} ",error.getMessage());
					return Mono.just(ChatResponse.builder().generations(List.of(
							generation
					)).build());
				}).map(chatResponse -> chatResponse.getResult().getOutput());
	}
}
