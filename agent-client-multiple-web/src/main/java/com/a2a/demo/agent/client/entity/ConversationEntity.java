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

import com.a2a.demo.agent.client.dto.Conversation;
import com.musaemotion.a2a.common.utils.GuidUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.entity
 * @project：A2A
 * @date：2025/5/6 17:16
 * @description：请完善描述
 */
@Entity
@Table(name = "conversation")
@Data
@NoArgsConstructor
public class ConversationEntity {

    /**
     * 主键id
     */
    @Id
    private String id;

    /**
     * 对话名称
     */
    private String name;

    /**
     * 是否激活状态
     */
    private Boolean isActive;

    /**
     * 对话名称
     * @param name
     * @return
     */
    public static ConversationEntity newConversation(String name, String conversationId) {
        ConversationEntity entity = new ConversationEntity();
        entity.setName(name);
        entity.setIsActive(true);
        entity.setId(conversationId);
        return entity;
    }

    /**
     * 对话名称
     * @param name
     * @return
     */
    public static ConversationEntity newConversation(String name) {
        ConversationEntity entity = new ConversationEntity();
        entity.setName(name);
        entity.setIsActive(true);
        entity.setId(GuidUtils.createShortRandomGuid());
        return entity;
    }

	/**
	 *
	 * @return
	 */
    public Conversation toDto(){
        Conversation conversation = new Conversation();
        BeanUtils.copyProperties(this, conversation);
        return conversation;
    }
}
