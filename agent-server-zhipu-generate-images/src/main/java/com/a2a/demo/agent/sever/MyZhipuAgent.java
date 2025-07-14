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

package com.a2a.demo.agent.sever;

import com.musaemotion.a2a.agent.server.agent.AgentGeneralResponse;
import com.musaemotion.a2a.agent.server.agent.AgentRequest;
import com.musaemotion.a2a.agent.server.agent.AgentResponseStatus;
import com.musaemotion.a2a.agent.server.agent.AgentService;
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import com.musaemotion.a2a.common.constant.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.zhipuai.ZhiPuAiImageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class MyZhipuAgent implements AgentService {

    private ZhiPuAiImageModel zhiPuAiImageModel;

    private A2aServerProperties a2aServerProperties;

    /**
     *
     * @param imageModel
     * @param a2aServerProperties
     */
    @Autowired
    public MyZhipuAgent(ZhiPuAiImageModel imageModel, A2aServerProperties a2aServerProperties){
       this.zhiPuAiImageModel = imageModel;
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
		return "zhipu";
	}

	/**
     * 流请求
     * @param agentRequest
     * @return
     */
    @Override
    public Flux<AgentGeneralResponse> stream(AgentRequest agentRequest)  {
       throw new NotImplementedException("该智能体未实现");
    }

    /**
     *
     * @param agentRequest
     * @return
     */
    @Override
    public AgentGeneralResponse call(AgentRequest agentRequest) {
        if (StringUtils.isEmpty(agentRequest.getText())) {
            return AgentGeneralResponse.fromText("请输入生成图片的要求", AgentResponseStatus.INPUT_REQUIRED);
        }
        ImagePrompt imagePrompt = new ImagePrompt(agentRequest.getText());
        ImageResponse imageResponse = this.zhiPuAiImageModel.call(imagePrompt);
        return AgentGeneralResponse.fromImageResponse(imageResponse);
    }
}
