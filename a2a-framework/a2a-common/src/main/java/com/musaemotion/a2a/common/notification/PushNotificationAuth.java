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

package com.musaemotion.a2a.common.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.server.notification
 * @project：A2A
 * @date：2025/4/22 16:31
 * @description：请完善描述
 */
@Slf4j
public class PushNotificationAuth {

    public static final String AUTH_HEADER_PREFIX = "Bearer ";

    public static final String AUTH_SCHEMES = "bearer";

    public static final String AUTH_HEADER_NAME = "Authorization";

    public static final String CLAIM_AGENT_NAME = "agent_name";

    public static final String CLAIM_BODY = "request_body_sha256";

    public static final String CLAIM_TIME = "iat";

    /**
     * 计算请求的BodySha256
     * @param data
     * @return
     */
    protected String calculateRequestBodySha256(Map<String, Object> data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String bodyStr = objectMapper.writeValueAsString(data);
        return this.calculateRequestBodySha256(bodyStr);
    }

    /**
     * 验证bodyStr
     * @param bodyStr
     * @return
     */
    protected String calculateRequestBodySha256(String bodyStr) {
        return Hashing.sha256().hashString(bodyStr, StandardCharsets.UTF_8).toString();
    }

}
