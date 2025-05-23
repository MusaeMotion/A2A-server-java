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

package com.musaemotion.a2a.agent.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musaemotion.a2a.common.AgentCard;
import com.musaemotion.a2a.common.exception.A2AClientHTTPException;
import com.musaemotion.a2a.common.exception.A2AClientJSONException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.client
 * @project：A2A
 * @date：2025/4/24 21:59
 * @description：agent Card 访问器
 */
@Slf4j
public class A2ACardResolver {

    private String baseUrl;

    private String agentCardPath;

    /**
     *
     * @param baseUrl
     * @param agentCardPath
     */
    public A2ACardResolver(String baseUrl, String agentCardPath) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.agentCardPath = agentCardPath.replaceAll("^/*", "");;
    }

    /**
     *
     * @param baseUrl
     */
    public A2ACardResolver(String baseUrl) {
      this(baseUrl, "/.well-known/agent.json");
    }

    /**
     * 获取agentCard 信息
     * @return
     */
    public AgentCard getAgentCard() {
        String url = baseUrl +"/"+ agentCardPath;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            ObjectMapper objectMapper = new ObjectMapper();
            HttpGet get = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(get)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                return objectMapper.readValue(responseBody, AgentCard.class);
            }
        } catch (IOException e) {
            log.error("getAgentCard Request Error", e);
            throw new A2AClientHTTPException(400,e.getMessage());
        }
        catch (ParseException e) {
            throw new A2AClientJSONException(e.getMessage());
        }
    }
}
