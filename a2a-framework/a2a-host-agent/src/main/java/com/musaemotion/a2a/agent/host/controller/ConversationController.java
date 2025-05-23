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

import com.musaemotion.a2a.agent.host.constant.ControllerSetting;
import com.musaemotion.a2a.agent.host.model.response.Result;
import com.musaemotion.a2a.agent.host.manager.HostAgentManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.controller
 * @project：A2A
 * @date：2025/5/6 18:32
 * @description：请完善描述
 */
@RestController
@Slf4j
@RequestMapping(ControllerSetting.CONVERSATION)
@RequiredArgsConstructor
public class ConversationController {


    private final HostAgentManager hostAgentManager;

    /**
     * 获取交谈列表
     * @return
     */
    @GetMapping
    public ResponseEntity list() {
        return ResponseEntity.ok(Result.buildSuccess(this.hostAgentManager.listConversation()));
    }

    /**
     * 删除交谈信息
     * @param conversationId
     * @return
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity delete(@PathVariable String conversationId) {
        this.hostAgentManager.deleteConversation(conversationId);
        return ResponseEntity.ok(Result.buildSuccess());
    }

    /**
     * 创建一个新的对话
     * @return
     */
    @PostMapping
    public ResponseEntity create() {
        return ResponseEntity.ok(
                Result.buildSuccess(
                        this.hostAgentManager.createConversation()
                )
        );
    }

    /**
     * 获取交谈相关的消息
     * @param conversationId
     * @return
     */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity messages(@PathVariable String conversationId) {
        return ResponseEntity.ok(Result.buildSuccess(
                this.hostAgentManager.listMessage(conversationId)
        ));
    }

}
