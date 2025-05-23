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

import com.a2a.demo.agent.client.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.repository
 * @project：A2A
 * @date：2025/5/9 16:47
 * @description：请完善描述
 */
@Repository
public interface TaskRepository  extends JpaRepository<TaskEntity, String>, JpaSpecificationExecutor<TaskEntity> {


    /**
     * @param conversationId
     * @return
     */
    List<TaskEntity> findByConversationIdOrderByCreatedAtDesc(String conversationId);

    /**
     * 获取所有
     * @return
     */
    List<TaskEntity> findAllByOrderByCreatedAtDesc();


    /**
     * 
     * @param messageIds
     * @return
     */
    List<TaskEntity> findAllByInputMessageIdIn(List<String> messageIds);

    /**
     * 删除对话相关的所有消息
     * @param conversationId
     * @return
     */
    Integer deleteByConversationId(String conversationId);
}
