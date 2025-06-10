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

package com.a2a.demo.agent.client.repository;

import com.a2a.demo.agent.client.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.repository
 * @project：A2A
 * @date：2025/5/7 15:25
 * @description：请完善描述
 */
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, String>, JpaSpecificationExecutor<MessageEntity> {

    /**
     * 获取交谈想换的消息
     * @param conversationId
     * @return
     */
     List<MessageEntity> findByConversationId(String conversationId);

    /**
     * 删除对话相关的所有消息
     * @param conversationId
     * @return
     */
    Integer deleteByConversationId(String conversationId);


    /**
     * 获取最后那条信息
     * @return
     */
    Optional<MessageEntity> findFirstByConversationIdOrderByCreatedAtDesc(String conversationId);
}
