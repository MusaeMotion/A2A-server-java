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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.server.agent
 * @project：A2A
 * @date：2025/4/27 16:19
 * @description：请完善描述
 */
public class AgentResponseStatusDeserializer extends JsonDeserializer<AgentResponseStatus> {
    @Override
    public AgentResponseStatus deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String value = jsonParser.getText().toUpperCase(); // 将输入值转换为大写
        for (AgentResponseStatus status : AgentResponseStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new JsonProcessingException("Unknown AgentResponseStatus: " + value) {};
    }
}
