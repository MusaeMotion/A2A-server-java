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

package com.a2a.demo.agent.client.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import com.musaemotion.a2a.common.base.Common;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

import static com.musaemotion.a2a.common.constant.MetaDataKey.*;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.entity
 * @project：A2A
 * @date：2025/5/7 15:22
 * @description：请完善描述
 */
@Entity
@Table(name = "message")
@Data
@NoArgsConstructor
public class MessageEntity {

    @Id
    private String id;

    /**
     * 交谈id
     */
    private String conversationId;

    /**
     * 消息内容
     */
    @Type(JsonType.class)
    @Column(length = 1000, columnDefinition = "json")
    private Common.Message message;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    public static MessageEntity newMessage(Common.Message message) {
        MessageEntity entity = new MessageEntity();
        entity.setMessage(message);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setId(message.getMetadata().get(MESSAGE_ID).toString());
        entity.setConversationId(message.getMetadata().get(CONVERSATION_ID).toString());
        return entity;
    }
}
