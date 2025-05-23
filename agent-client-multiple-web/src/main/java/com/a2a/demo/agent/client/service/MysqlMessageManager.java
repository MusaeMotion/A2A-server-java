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

package com.a2a.demo.agent.client.service;

import com.a2a.demo.agent.client.entity.MessageEntity;
import com.a2a.demo.agent.client.repository.MessageRepository;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.agent.host.manager.AbstractMessageManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.service
 * @project：A2A
 * @date：2025/5/7 15:27
 * @description：请完善描述
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MysqlMessageManager extends AbstractMessageManager {

    private final MessageRepository messageRepository;

    @Override
    public void add(Common.Message message) {
        MessageEntity messageEntity = MessageEntity.newMessage(message);
        this.messageRepository.save(messageEntity);
    }

    @Override
    public List<Common.Message> listByConversationId(String conversationId) {
        return this.messageRepository.findByConversationId(conversationId).stream()
                .sorted(Comparator.comparing(MessageEntity::getCreatedAt))
                .map(MessageEntity::getMessage)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Common.Message> getByMessageId(String messageId) {
        var op = messageRepository.findById(messageId);
        if (op.isPresent()) {
            return Optional.of(op.get().getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Common.Message> lastByConversationId(String conversationId) {
        var op = this.messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId);
        if (op.isPresent()) {
             return Optional.of(op.get().getMessage());
        }
        return Optional.empty();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteByConversationId(String conversationId) {
        this.messageRepository.deleteByConversationId(conversationId);
    }
}
