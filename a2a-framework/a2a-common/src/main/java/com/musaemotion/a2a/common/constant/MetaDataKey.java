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

package com.musaemotion.a2a.common.constant;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.constant
 * @project：A2A
 * @date：2025/4/28 16:52
 * @description：请完善描述
 */
public interface MetaDataKey {

    /**
     * 当前任务id
     */
    String TASK_ID = "task_id";

    /**
     * 当前智能体名称
     */
    String CUR_AGENT_NAME = "agentName";

    /**
     * 就是 conversation_id 交谈id
     */
    String SESSION_ID = "session_id";

    /**
     * 交谈id, 就是sessionId, 估计是adk里面会读取 conversation_id 字段
     */
    String CONVERSATION_ID = "conversation_id";

    /**
     * 输入消息扩展元数据
     */
    String INPUT_MESSAGE_METADATA = "input_message_metadata";

    /**
     * 消息id
     */
    String MESSAGE_ID = "message_id";

    /**
     * 上一条消息id
     */
    String LAST_MESSAGE_ID = "last_message_id";

    /**
     * 当前session 活动 状态
     */
    String SESSION_ACTIVE = "session_active";


}
