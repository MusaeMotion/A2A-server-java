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

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.server.model
 * @project：A2A
 * @date：2025/4/22 15:29
 * @description：请完善描述
 */
@JsonDeserialize(using = AgentResponseStatusDeserializer.class)
public enum AgentResponseStatus {

    INPUT_REQUIRED("INPUT_REQUIRED"),

    COMPLETED("COMPLETED"),

    WORKING("WORKING"),

    ERROR("ERROR");

    private String value;

    AgentResponseStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
    @JsonValue
    public void setValue(String input) {
        this.value = input;
    }
    @Override
    public String toString() {
        return value;
    }
}
