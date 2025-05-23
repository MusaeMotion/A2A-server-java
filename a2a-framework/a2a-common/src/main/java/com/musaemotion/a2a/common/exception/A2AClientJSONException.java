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

package com.musaemotion.a2a.common.exception;

import lombok.Getter;
import lombok.ToString;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.common.exception
 * @project：A2A
 * @date：2025/4/24 22:21
 * @description：请完善描述
 */
@ToString
public class A2AClientJSONException extends A2AClientException {

    private String message;

    @Getter
    private String json;

    public A2AClientJSONException(String message, String json) {
        super("JSON Error: "+message);
        this.message = message;
        this.json = json;
    }

    public A2AClientJSONException(String message) {
        super("JSON Error: "+message);
        this.message = message;
    }
}
