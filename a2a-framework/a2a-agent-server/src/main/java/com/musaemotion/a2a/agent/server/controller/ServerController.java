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

package com.musaemotion.a2a.agent.server.controller;


import com.musaemotion.a2a.common.AgentCard;
import com.musaemotion.a2a.agent.server.notification.PushNotificationSenderService;
import com.musaemotion.a2a.common.base.base.JSONRPCMessage;
import com.musaemotion.a2a.agent.server.manager.ServerEndpointManager;
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import com.musaemotion.a2a.agent.server.agent.AgentService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;


/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：Endpoint
 */
@RestController
@RequiredArgsConstructor
public class ServerController {

	private final A2aServerProperties a2aServerProperties;

	private final ServerProperties serverProperties;

	private final String urlTemplate = "http://%s:%s/";

	private final ServerEndpointManager serverEndpointManager;

	private final PushNotificationSenderService pushNotificationSenderService;

	/**
	 * 智能体说明
	 * TODO 后续完善鉴权功能 AgentAuthentication，让hostAgent访问的时候增加授权访问的能力
	 * @return
	 */
	@GetMapping(value = {"/.well-known/agent.json"})
	@ResponseBody
	public AgentCard wellKnownAgentCard() {
		AgentCard agentCard = AgentCard.builder().build();
		BeanUtils.copyProperties(this.a2aServerProperties, agentCard);
		if (!StringUtils.hasText(agentCard.getUrl())) {
			agentCard.setUrl(
					String.format(urlTemplate,
							serverProperties.getAddress().getHostAddress(),
							serverProperties.getPort()
					)
			);
		}
		return agentCard;
	}

	/**
	 * 智能体jwk密钥终结点。
	 * @return
	 */
	@GetMapping(value = {"/.well-known/jwks.json"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String wellKnownAgentJwk() {
		return pushNotificationSenderService.getJwk();
	}

	/**
	 * 如果json请求走这个接口
	 * TODO 后续完善鉴权功能 AgentAuthentication，让hostAgent访问的时候增加授权访问的能力
	 * @param request
	 * @return
	 */
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public JSONRPCMessage processRequest(@RequestBody LinkedHashMap request) {
		return this.serverEndpointManager.processRequest(request);
	}

	/**
	 * 如果是 流模式 accept text/event-stream
	 * TODO 后续完善鉴权功能 AgentAuthentication，让hostAgent访问的时候增加授权访问的能力
	 * @param request
	 * @return
	 */
	@PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<?> processRequestSubscribe(@RequestBody LinkedHashMap request) {
		return this.serverEndpointManager.processRequestSubscribe(request);
	}
}
