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

package com.musaemotion.a2a.agent.host.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.musaemotion.a2a.agent.host.manager.SseEmitterManager;
import com.musaemotion.a2a.agent.host.model.response.CommonMessageExt;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.agent.host.constant.ControllerSetting;
import com.musaemotion.a2a.agent.host.model.response.Result;
import com.musaemotion.a2a.agent.host.model.response.SendMessageResponse;
import com.musaemotion.a2a.agent.host.manager.HostAgentManager;
import com.musaemotion.a2a.common.constant.MessageRole;
import com.musaemotion.a2a.common.utils.GuidUtils;
import com.musaemotion.agent.model.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.controller
 * @project：A2A
 * @date：2025/4/28 11:55
 * @description：chat 控制器
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping(ControllerSetting.CHAT)
@RestController
public class ChatController {

    /**
     * 主机智能体
     */
    private final HostAgentManager hostAgentManager;

	/**
	 * 消息订阅
	 * @param conversationId
	 * @param inputMessageId
	 * @return
	 */
	@GetMapping(value = "/notification/{conversationId}/{inputMessageId}")
	public SseEmitter notification(@PathVariable("conversationId") String conversationId, @PathVariable("inputMessageId") String inputMessageId) {
		return SseEmitterManager.subscribe(conversationId, inputMessageId);
	}

    /**
     * call 同步调用
     * @param input
     * @return
     */
    @PostMapping(value = "/call")
    public ResponseEntity call(@RequestBody SendMessageRequest input) {
		try {
			return ResponseEntity.ok(
					Result.buildSuccess(
							this.hostAgentManager.call(input)
					)
			);
		} catch (Exception e) {
			return ResponseEntity.ok(Result.buildSuccess("出现异常了"));
		}
	}

    /**
     * stream 流调用
     * @param input
     * @return
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SendMessageResponse> stream(@RequestBody SendMessageRequest input) {
        return  this.hostAgentManager.stream(input);
    }

}
