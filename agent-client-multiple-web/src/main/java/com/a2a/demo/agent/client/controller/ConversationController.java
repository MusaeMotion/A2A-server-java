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

package com.a2a.demo.agent.client.controller;

import com.a2a.demo.agent.client.configuration.HostAgentConfig;
import com.a2a.demo.agent.client.dto.Conversation;
import com.a2a.demo.agent.client.service.MysqlConversationManager;
import com.a2a.demo.agent.client.service.MysqlMessageManager;
import com.a2a.demo.agent.client.service.MysqlTaskCenterManager;
import com.musaemotion.a2a.agent.host.constant.ControllerSetting;
import com.musaemotion.a2a.agent.host.model.response.CommonMessageExt;
import com.musaemotion.a2a.agent.host.model.response.Result;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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


	private final MysqlConversationManager conversationManager;

	private final MysqlMessageManager messageManager;

	private final MysqlTaskCenterManager taskCenterManager;

	private final ChatMemoryRepository chatMemoryRepository;
    /**
     * 获取交谈列表
     * @return
     */
    @GetMapping
    public ResponseEntity list() {
        return ResponseEntity.ok(Result.buildSuccess(this.conversationManager.list()));
    }


	/**
	 * 删除
 	 * @param conversationId
	 */
	private void deleteConversation(String conversationId) {
		this.messageManager.deleteByConversationId(conversationId);
		this.taskCenterManager.deleteByConversationId(conversationId);
		this.chatMemoryRepository.deleteByConversationId(conversationId);
	}

    /**
     * 删除交谈信息
     * @param conversationId
     * @return
     */
    @DeleteMapping("/{conversationId}")
	@Transactional(rollbackOn = Exception.class)
    public ResponseEntity delete(@PathVariable String conversationId) {
		this.conversationManager.delete(conversationId);
	    this.deleteConversation(conversationId);
        return ResponseEntity.ok(Result.buildSuccess());
    }

	/**
	 * 删除交谈以外的其他信息
	 * @param conversationId
	 * @return
	 */
	@DeleteMapping("/other/{conversationId}")
	@Transactional(rollbackOn = Exception.class)
	public ResponseEntity deleteOther(@PathVariable String conversationId) {
		this.deleteConversation(conversationId);
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
						this.conversationManager.create("")
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

		var messages = this.messageManager.listByConversationId(conversationId);

		List<CommonMessageExt> newMessages = messages.stream().map(item->CommonMessageExt.fromMessage(item)).collect(Collectors.toUnmodifiableList());

		List<String> messageIds = messages.stream()
				.filter(item->item.getLastMessageId()!=null)
				.map(Common.Message::getLastMessageId).distinct()
				.collect(Collectors.toUnmodifiableList());

		List<Task> tasks = this.taskCenterManager.listByInputMessageId(messageIds);
		newMessages.forEach(message->{
			var ts =  tasks.stream().filter(task -> task.getInputMessageId().equals(message.getLastMessageId())).collect(Collectors.toUnmodifiableList());
			if(!CollectionUtils.isEmpty(ts)){
				message.setTask(ts);
			}
			// 计算费用
			message.calAmount(HostAgentConfig.calculateAmount);
		});

        return ResponseEntity.ok(
				Result.buildSuccess(
				  newMessages
				)
		);
    }

}
