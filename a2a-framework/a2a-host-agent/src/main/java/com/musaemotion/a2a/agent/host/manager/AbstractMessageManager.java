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

package com.musaemotion.a2a.agent.host.manager;

import com.musaemotion.a2a.common.base.Common;

import java.util.List;
import java.util.Optional;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.service
 * @project：A2A
 * @date：2025/5/7 14:46
 * @description：请完善描述
 */
public abstract class AbstractMessageManager {

    /**
     * 添加消息 message 元数据里有交谈id和消息id
     * @param message
     */
    public abstract void upsert(Common.Message message);

    /**
     * 获取一条消息
     * @param messageId
     * @return
     */
    public abstract Optional<Common.Message> getByMessageId(String messageId);

    /**
     * 获取最后那条信息
     * @param conversationId
     * @return
     */
    public abstract Optional<Common.Message> lastByConversationId(String conversationId);

    /**
     * 删除相关对话的所有消息
     * @param conversationId
     */
    public abstract void deleteByConversationId(String conversationId);
}
