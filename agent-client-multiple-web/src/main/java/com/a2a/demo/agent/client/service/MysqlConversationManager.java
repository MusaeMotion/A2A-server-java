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

import com.a2a.demo.agent.client.entity.ConversationEntity;
import com.a2a.demo.agent.client.repository.ConversationRepository;
import com.a2a.demo.agent.client.dto.Conversation;
import com.musaemotion.a2a.agent.host.manager.AbstractConversationManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.service
 * @project：A2A
 * @date：2025/5/6 17:25
 * @description：请完善描述
 */
@Service
@Slf4j
public class MysqlConversationManager extends AbstractConversationManager {

    /**
     * 对话持久层
     */
    private ConversationRepository repository;

    /**
     * 构造函数
     * @param repository
     */
    @Autowired
    public MysqlConversationManager(ConversationRepository repository){
        this.repository = repository;
    }


	/**
	 * 创建交谈
	 * @param conversationName
	 * @return
	 */
    public Conversation create(String conversationName) {
        ConversationEntity conversationEntity = ConversationEntity.newConversation(conversationName);
        this.repository.save(conversationEntity);
        return conversationEntity.toDto();
    }

    /**
     * 已经存在
     * @param conversationId
     * @return
     */
    @Override
    public Boolean exist(String conversationId) {
        return this.repository.existsById(conversationId);
    }

	/**
	 * 删除一个交谈信息
	 * @param conversationId
	 */
    @Transactional(rollbackOn = Exception.class)
    public void delete(String conversationId) {
       this.repository.deleteById(conversationId);
    }

    /**
     * 获取交谈列表
     * @return
     */
    public List<Conversation> list() {
        return this.repository.findAll().stream().map(item -> item.toDto()).collect(Collectors.toList());
    }
}
