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

package com.musaemotion.a2a.agent.client.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.musaemotion.a2a.agent.client.INotificationConsumer;
import com.musaemotion.a2a.agent.client.VerifyPushNotificationDto;
import com.musaemotion.a2a.agent.client.notification.PushNotificationReceiverAuth;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.AbstractReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.musaemotion.a2a.common.notification.PushNotificationAuth.AUTH_HEADER_NAME;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client
 * @project：A2A
 * @date：2025/4/25 10:48
 * @description：接受消息通知Server
 */
@Slf4j
public class PushNotificationServer {


    public final static String NOTIFY_URL_TPL = "http://%s:%s%s";

    @Getter
    private final InetAddress host;

    @Getter
    private final int port;

    // 推送授权组件
    private final PushNotificationReceiverAuth pushNotificationReceiverAuth = new PushNotificationReceiverAuth();

    // 通知服务路径
    public static final String NOTIFY_PATH = "/notify";

    // agent jwsk 路径
    private final String JWKS_PATH = "/.well-known/jwks.json";

    // 智能体名称和智能体url对
    private Map<String, String> agentUrls;

    // 消息消费者列表，可以注册多个消费者
	private List<INotificationConsumer> notificationConsumers;

	// 外部通知地址
	private String externalUrl;

    /**
     * @param host 通知服务的host
     * @param port 通知服务的端口
     */
    public PushNotificationServer(InetAddress host, int port, List<INotificationConsumer> notificationConsumers, String externalUrl) {
        this.host = host;
        this.port = port;
        this.agentUrls = Maps.newHashMap();
		this.notificationConsumers = notificationConsumers;
		this.externalUrl = externalUrl;
    }

    /**
     * 刷新添加所有的终结点
     */
    public void refreshAgentUrls() {
        this.agentUrls.forEach((agenName, agentUrl) -> {
            this.pushNotificationReceiverAuth.registerAgentJwkEndpoint(agenName, agentUrl.replaceAll("/+$", "") + JWKS_PATH);
        });
    }

    /**
     * 添加智能体
     * @param agenName
     * @param agentUrl
     */
    public void registerAgent(String agenName, String agentUrl) {
        this.agentUrls.put(agenName, agentUrl);
        this.pushNotificationReceiverAuth.registerAgentJwkEndpoint(agenName, agentUrl.replaceAll("/+$", "") + JWKS_PATH);
    }

    /**
     * 启动web服务
     */
    public void strat()  {

        HttpHandler httpHandler = RouterFunctions.toHttpHandler(this.notificationRoutes());
        ReactiveWebServerFactory factory = null;
        try {
            factory = this.webServerFactory();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        // 启动服务器
        factory.getWebServer(httpHandler).start();

    }


	/**
	 * 获取外部访问url
	 * @return
	 */
	public String getExternalUrl(){
        return this.externalUrl.replaceAll("/+$", "")+"/"+PushNotificationServer.NOTIFY_PATH;
    }


    /**
     * 构建通知服务路由
     * @return
     */
    public RouterFunction<ServerResponse> notificationRoutes() {
		return RouterFunctions.route()
				.GET(NOTIFY_PATH, this::handleValidationCheck)
				.POST(NOTIFY_PATH, this::handleNotification)
				.build();
    }

    /**
     * 验证联通性，获取到validationToken ，直接返回给请求者
     * @param request
     * @return
     */
    private Mono<ServerResponse> handleValidationCheck(ServerRequest request) {
        String validationToken = request.queryParam("validationToken").orElse(null);
        if (validationToken == null) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST).build();
        }
        return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).bodyValue(validationToken);
    }


	/**
	 * 处理通知
	 * @param request
	 * @return
	 */
	private Mono<ServerResponse> handleNotification(ServerRequest request) {
		return request.bodyToMono(String.class)
				.flatMap(data -> verifyAndProcessNotification(request, data))
				.onErrorResume(e -> {
					log.error("Error verifying push notification: {}", e.getMessage());
					return handleNotificationError(e);
				});
	}
	/**
	 * 处理错误
	 * @param e
	 * @return
	 */
	private Mono<ServerResponse> handleNotificationError(Throwable e) {
		return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
	/**
	 * 验证并处理通知
	 * @param request
	 * @param data
	 * @return
	 */
	private Mono<ServerResponse> verifyAndProcessNotification(ServerRequest request, String data) {
		String authHeader = request.headers().firstHeader(AUTH_HEADER_NAME);
		try {
			VerifyPushNotificationDto verifyPushNotification = this.pushNotificationReceiverAuth.verifyPushNotification(authHeader, data);
			if (verifyPushNotification.getSuccess()) {
				this.processNotification(data, verifyPushNotification.getAgentName());
				return ServerResponse.ok().build();
			} else {
				log.warn("Signature verification failed for push notification data: {}", data);
				return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
			}
		} catch (Exception e) {
			log.error("Error during push notification verification: {}", e.getMessage());
			return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * 处理通知数据
	 * @param data
	 */
	private void processNotification(String data, String agentName) {
		if (!CollectionUtils.isEmpty(this.notificationConsumers)) {
			this.notificationConsumers.forEach(c -> c.processMessage(data, agentName));
		}
	}


    /**
     * 创建一个Netty服务 Undertow 服务其实也可以
     *
     * @return
     * @throws UnknownHostException
     */
    private ReactiveWebServerFactory webServerFactory() throws UnknownHostException {
        AbstractReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
        factory.setPort(this.port);
        factory.setAddress(this.host);
        return factory;
    }


}
