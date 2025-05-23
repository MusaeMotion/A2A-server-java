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

package com.musaemotion.a2a.common.event;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.musaemotion.a2a.common.base.Common;

import java.io.IOException;
import java.util.Map;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.common.event
 * @project：A2A
 * @date：2025/4/27 15:23
 * @description：请完善描述
 */
public class TaskEventDeserializer extends JsonDeserializer<TaskEvent> {
    @Override
    public TaskEvent deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        // 获取当前的 JsonNode
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        // 根据 JSON 字段动态选择子类
        if (node.has("artifact")) {
            // 如果存在 artifact 字段，则构造 TaskArtifactUpdateEvent
            TaskArtifactUpdateEvent event = TaskArtifactUpdateEvent.builder()
                    .id(node.get("id").asText())
                    .artifact(node.get("artifact").traverse(jsonParser.getCodec()).readValueAs(Common.Artifact.class))
                    .metadata(node.has("metadata")? node.get("metadata").traverse(jsonParser.getCodec()).readValueAs(new TypeReference<Map<String, Object>>() {}): null)
                    .build();
            return event;
        } else if (node.has("status")) {
            // 如果存在 status 字段，则构造 TaskStatusUpdateEvent
            TaskStatusUpdateEvent event = TaskStatusUpdateEvent.builder()
                    .id(node.get("id").asText())
                    .metadata(node.has("metadata")? node.get("metadata").traverse(jsonParser.getCodec()).readValueAs(new TypeReference<Map<String, Object>>() {}): null)
                            .status(node.get("status").traverse(jsonParser.getCodec()).readValueAs(Common.TaskStatus.class))
                                    .done(node.get("final").asBoolean())
                    .build();
            return event;
        } else {
            // 如果没有特定字段，抛出异常或返回一个默认的 TaskEvent
            throw new JsonProcessingException("Unknown TaskEvent type") {};
        }
    }
}
