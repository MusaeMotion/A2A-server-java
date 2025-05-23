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

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.common.exception
 * @project：A2A
 * @date：2025/4/24 22:23
 * @description：请完善描述
 */
@ToString
@EqualsAndHashCode(callSuper=true)
public class A2AClientHTTPException extends A2AClientException{

    private Integer statusCode;

    private String message;

    public A2AClientHTTPException(Integer statusCode, String message) {
        super("HTTP Error "+statusCode+": "+message);
        this.message = message;
        this.statusCode = statusCode;
    }
}
