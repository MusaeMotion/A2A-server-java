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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.UsageTokens;
import com.musaemotion.a2a.common.constant.MediaType;
import com.musaemotion.a2a.common.utils.GuidUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageResponse;

import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.server.agent
 * @project：A2A
 * @date：2025/5/12 11:38
 * @description：请完善描述
 */
@Data
@Slf4j
public class AgentGeneralResponse {

    /**
     * 智能体运行的状态
     */
    private AgentResponseStatus status;

    /**
     * 内容
     */
    private List<Common.Part> parts;

	/**
	 * token使用数
	 */
	private UsageTokens usageTokens = new UsageTokens();

    /**
     * 获取part内容
     * @return
     * @throws JsonProcessingException
     */
    public String getTextPartContent() throws JsonProcessingException {
        if (parts == null){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Common.Part part : parts){
            if(part instanceof Common.TextPart textPart){
                sb.append(textPart.getText());
            }
            if(part instanceof  Common.DataPart dataPart){
                ObjectMapper mapper = new ObjectMapper();
                sb.append(mapper.writeValueAsString(dataPart.getData()));
            }
        }
        return sb.toString();
    }

	/**
	 * 创建 AgentGeneralResponse
	 * @param agentTextResponse
	 * @return
	 */
    private static AgentGeneralResponse fromAgentTextResponse(AgentTextResponse agentTextResponse) {
        AgentGeneralResponse agentGeneralResponse = new AgentGeneralResponse();
        agentGeneralResponse.status = agentTextResponse.getStatus();
        agentGeneralResponse.setParts(Lists.newArrayList(new Common.TextPart(agentTextResponse.getContent())));
        return agentGeneralResponse;
    }


	/**
	 * 根据chatResponse构建 AgentGeneralResponse 对象, 加入了 token计算对象
	 * @param chatResponse
	 * @return
	 */
	public static AgentGeneralResponse fromCallChatResponse(ChatResponse chatResponse) {
		BeanOutputConverter<AgentTextResponse> converter = new BeanOutputConverter<>(AgentTextResponse.class);
		AgentTextResponse agentTextResponse = converter.convert(chatResponse.getResult().getOutput().getText());
		var agentGeneralResponse = AgentGeneralResponse.fromAgentTextResponse(agentTextResponse);
		var usage = chatResponse.getMetadata().getUsage();
		log.info("promptTokens: {}, completionTokens: {}, totalTokens: {}",
				usage.getPromptTokens(),
				usage.getCompletionTokens(),
				usage.getTotalTokens()
		);
		agentGeneralResponse.setUsageTokens(
				UsageTokens.fromUsage(
					usage.getCompletionTokens(),
					usage.getPromptTokens(),
					usage.getTotalTokens()
		));
		return agentGeneralResponse;
	}

	/**
	 * 根据chatResponse构建 AgentGeneralResponse 对象, 加入了 token计算对象
	 * @param chatResponse
	 * @return
	 */
	public static AgentGeneralResponse fromCallChatResponse(ChatResponse chatResponse, AgentResponseStatus status) {
		var agentGeneralResponse = fromCallChatResponse(chatResponse);
		agentGeneralResponse.status = status;
		return agentGeneralResponse;
	}

	/**
	 * 获取流聊天对象
	 * @param chatResponse
	 * @param status
	 * @return
	 */
	public static AgentGeneralResponse fromStreamChatResponse(ChatResponse chatResponse, AgentResponseStatus status) {

		if(chatResponse.getResult()!=null){
			AgentTextResponse agentTextResponse = new AgentTextResponse();
			agentTextResponse.setContent(chatResponse.getResult().getOutput().getText());
			agentTextResponse.setStatus(status);
			AgentGeneralResponse agentGeneralResponse = fromAgentTextResponse(agentTextResponse);
			// TODO RateLimit rateLimit = chatResponse.getMetadata().getRateLimit();
			// 限流对象
			Usage usage = chatResponse.getMetadata().getUsage();
			agentGeneralResponse.setUsageTokens(
					UsageTokens.fromUsage(
							usage.getCompletionTokens(),
							usage.getPromptTokens(),
							usage.getTotalTokens()
					));
			return agentGeneralResponse;
		}
		Usage usage = chatResponse.getMetadata().getUsage();
		AgentGeneralResponse agentGeneralResponse = AgentGeneralResponse.fromText("", AgentResponseStatus.COMPLETED);
		agentGeneralResponse.setUsageTokens(
				UsageTokens.fromUsage(
						usage.getCompletionTokens(),
						usage.getPromptTokens(),
						usage.getTotalTokens()
				));
		return agentGeneralResponse;
	}
	/**
	 * 创建AgentGeneralResponse
	 * @param text
	 * @param status
	 * @return
	 */
    public static AgentGeneralResponse fromText(String text, AgentResponseStatus status) {
        AgentGeneralResponse agentGeneralResponse = new AgentGeneralResponse();
        agentGeneralResponse.status = status;
        agentGeneralResponse.setParts(Lists.newArrayList(new Common.TextPart(text)));
        return agentGeneralResponse;
    }

	/**
	 *
	 * @param text
	 * @param status
	 * @param usageTokens
	 * @return
	 */
	public static AgentGeneralResponse fromText(String text, AgentResponseStatus status, UsageTokens usageTokens) {
		AgentGeneralResponse agentGeneralResponse = fromText(text, status);
		agentGeneralResponse.setUsageTokens(usageTokens);
		return agentGeneralResponse;
	}

	/**
	 * AgentGeneralResponse
	 * @param imageResponse
	 * @return
	 */
    public static AgentGeneralResponse fromImageResponse(ImageResponse imageResponse) {
        AgentGeneralResponse agentGeneralResponse = new AgentGeneralResponse();
        agentGeneralResponse.status = AgentResponseStatus.COMPLETED;
        Image image = imageResponse.getResult().getOutput();
        Common.FilePart filePart = Common.FilePart.newFilePart(
                Common.FileContent.builder()
                        .name(GuidUtils.createShortRandomGuid())
                        .mimeType(MediaType.IMAGE_PNG.getValue())
                        .uri(image.getUrl())
                        .bytes(image.getB64Json())
                        .build()
        );
        agentGeneralResponse.setParts(Lists.newArrayList(filePart));
		// 图片生成一般按次计算
		agentGeneralResponse.setUsageTokens(UsageTokens.fromUsage(imageResponse.getResults().size()));
        return agentGeneralResponse;
    }
}
