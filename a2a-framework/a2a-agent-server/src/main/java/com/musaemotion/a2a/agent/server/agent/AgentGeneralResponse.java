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
import com.musaemotion.a2a.common.constant.MediaType;
import com.musaemotion.a2a.common.utils.GuidUtils;
import lombok.Data;
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
public class AgentGeneralResponse {

    /**
     * 状态
     */
    private AgentResponseStatus status;

    /**
     * 内容
     */
    private List<Common.Part> parts;

    /**
     * 获取part内容
     * @return
     * @throws JsonProcessingException
     */
    public String getPart() throws JsonProcessingException {
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

    public static AgentGeneralResponse fromAgentTextResponse(AgentTextResponse agentTextResponse) {
        AgentGeneralResponse agentGeneralResponse = new AgentGeneralResponse();
        agentGeneralResponse.status = agentTextResponse.getStatus();
        agentGeneralResponse.setParts(Lists.newArrayList(new Common.TextPart(agentTextResponse.getContent())));
        return agentGeneralResponse;
    }

    public static AgentGeneralResponse fromText(String text, AgentResponseStatus status) {
        AgentGeneralResponse agentGeneralResponse = new AgentGeneralResponse();
        agentGeneralResponse.status = status;
        agentGeneralResponse.setParts(Lists.newArrayList(new Common.TextPart(text)));
        return agentGeneralResponse;
    }

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
        return agentGeneralResponse;
    }
}
