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

import lombok.ToString;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.common.exception
 * @project：A2A
 * @date：2025/4/24 22:18
 * @description：请完善描述
 */
@ToString
public class A2AClientException extends RuntimeException {

    private static final long serialVersionUID = 194906846739586856L;

    public A2AClientException(String message) {
        super(message);
    }

    public A2AClientException(String message,  Throwable cause) {
        super(message, cause);
    }
}
