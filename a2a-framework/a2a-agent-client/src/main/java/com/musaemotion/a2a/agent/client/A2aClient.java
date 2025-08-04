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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musaemotion.a2a.common.AgentCard;
import com.musaemotion.a2a.common.exception.A2AClientHTTPException;
import com.musaemotion.a2a.common.exception.A2AClientJSONException;
import com.musaemotion.a2a.common.request.*;
import com.musaemotion.a2a.common.response.*;
import com.musaemotion.a2a.common.base.base.JSONRPCRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.springframework.http.HttpHeaders;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.client
 * @project：A2A
 * @date：2025/4/24 15:28
 * @description： A2a Agent 访问客户端
 */
@Slf4j
public class A2aClient {

    /**
     * 智能体url
     */
    private String url;


    /**
     * 私有构造
     * @param agentCard
     * @param url
     */
    private A2aClient(AgentCard agentCard, String url) {
        if (agentCard != null) {
            this.url = agentCard.getUrl();
            return;
        }
        if (StringUtils.hasText(url)) {
            this.url = url;
            return;
        }
        throw new RuntimeException("url is null");
    }

    /**
     * AgentCard
     * @param agentCard
     */
    public A2aClient(AgentCard agentCard) {
       this(agentCard, null);
    }

    /**
     * url
     * @param url
     */
    public A2aClient(String url) {
        this(null, url);
    }

	/**
	 * 发送同步请求
	 * @param sendTaskRequest
	 * @param headers
	 * @return
	 */
	public SendTaskResponse sendTask(SendTaskRequest sendTaskRequest, Map<String,String> headers) {
        return this.sendRequest(sendTaskRequest, headers, SendTaskResponse.class).join();
    }

	/**
	 * 发送流请求
	 * @param sendTaskStreamingRequest
	 * @param headers
	 * @return
	 */
	public ConnectableFlux<SendTaskStreamingResponse> sendTaskStreaming(SendTaskStreamingRequest sendTaskStreamingRequest, Map<String,String> headers) {
        return this.sendTaskStreaming(sendTaskStreamingRequest, headers, SendTaskStreamingResponse.class);
    }

    /**
     * 获取任务信息
     * @param getTaskRequest
     * @return
     */
    public GetTaskResponse getTask(GetTaskRequest getTaskRequest) {
		// 没有透传token
        return this.sendRequest(getTaskRequest, Map.of(), GetTaskResponse.class).join();
    }

    /**
     * 取消
     * @param payload
     * @return
     */
    public CancelTaskResponse cancelTask(LinkedHashMap payload){
        ObjectMapper objectMapper = new ObjectMapper();
        CancelTaskRequest cancelTaskRequest = objectMapper.convertValue(payload, CancelTaskRequest.class);
		// 没有透传token
        return this.sendRequest(cancelTaskRequest, Map.of(), CancelTaskResponse.class).join();
    }

    /**
     * 设置任务订阅回调
     * @param payload
     * @return
     */
    public SetTaskPushNotificationResponse setTaskCallback(LinkedHashMap payload){
        ObjectMapper objectMapper = new ObjectMapper();
        SetTaskPushNotificationRequest setTaskPushNotificationRequest = objectMapper.convertValue(payload, SetTaskPushNotificationRequest.class);
		// 没有透传token
        return this.sendRequest(setTaskPushNotificationRequest, Map.of(),  SetTaskPushNotificationResponse.class).join();
    }

    /**
     * 获取任务回调
     * @param payload
     * @return
     */
    public GetTaskPushNotificationResponse getTaskCallback(LinkedHashMap payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        GetTaskPushNotificationRequest getTaskPushNotificationRequest = objectMapper.convertValue(payload, GetTaskPushNotificationRequest.class);
		// 没有透传token
        return this.sendRequest(getTaskPushNotificationRequest, Map.of(),  GetTaskPushNotificationResponse.class).join();
    }


	/**
	 * 发送远程智能体流请求
	 * @param request
	 * @param headers
	 * @param clazz
	 * @return
	 * @param <T>
	 */
    private <T> ConnectableFlux<T> sendTaskStreaming(JSONRPCRequest request, Map<String,String> headers, Class<T> clazz) {

		// 构造 header 写入器
		Consumer<HttpHeaders> headerWriter = httpHeaders -> {
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			if (headers != null) {
				headers.forEach((k, v) -> {
					if (k != null && v != null) {
						httpHeaders.add(k, v);
					}
				});
			}
		};

		Flux flux = Flux.create(sink -> {
            WebClient httpClient = WebClient.create(this.url);
            Flux<String> eventStream = null;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonBody = objectMapper.writeValueAsString(request);
                eventStream = httpClient.post()
                        .bodyValue(jsonBody)
						.headers(headerWriter)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .retrieve()
                        .bodyToFlux(String.class);
            } catch (Exception e) {
                sink.error(new A2AClientHTTPException(400, e.getMessage()));
                return;
            }

            eventStream.subscribe(
                    content -> {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                        try {
                            T t = objectMapper.readValue(content.toString(), clazz);
                            sink.next(t);
                        } catch (JsonProcessingException e) {
                            throw new A2AClientJSONException(e.getMessage().toString(), content.toString());
                        }
                    },
                    err -> {
                        log.error("A2aClient Error occurred while sending event：{} ", err.getMessage());
                        sink.complete();
                    },
                    () -> {
                        sink.complete();
                    });
        });

        return flux.publish();
    }


	/**
	 * 发送远程智能体同步请求
	 * @param request
	 * @param headers
	 * @param clazz
	 * @return
	 * @param <T>
	 */
	private <T> CompletableFuture<T> sendRequest(JSONRPCRequest request, Map<String,String> headers, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonBody = objectMapper.writeValueAsString(request);
                HttpPost post = new HttpPost(this.url);

				// 1️⃣ 设置 Content-Type
				post.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
				// 2️⃣ 批量添加透传头（null / empty 安全）
				if (headers != null) {
					headers.entrySet().stream()
							.filter(e -> e.getKey() != null && e.getValue() != null)
							.forEach(e -> post.setHeader(e.getKey(), e.getValue()));
				}
                post.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                try (CloseableHttpResponse response = httpClient.execute(post)) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    return objectMapper.readValue(responseBody, clazz);
                }
            } catch (ParseException e) {
                throw new A2AClientJSONException(e.getMessage().toString());
            }
            catch (IOException e) {
                throw new A2AClientHTTPException(400, e.getMessage());
            }
        });
    }
}
