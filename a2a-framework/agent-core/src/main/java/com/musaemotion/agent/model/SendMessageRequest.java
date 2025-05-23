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

package com.musaemotion.agent.model;

import com.musaemotion.a2a.common.base.Common;
import lombok.Data;

import java.util.Map;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.framework.model
 * @project：A2A
 * @date：2025/4/28 13:18
 * @description：请完善描述
 */
@Data
public class SendMessageRequest {
    // 消息参数
    private Common.Message params;

    public String getConversationId() {
        return this.params.getConversationId();
    }

    public String getMessageId() {
        return this.params.getMessageId();
    }

    public String getLastMessageId() {
        return this.params.getLastMessageId();
    }

    /**
     * 默认part第一条文本消息消息为用户输入内容
     * @return
     */
    public String getContent() {
        if (this.params.getParts().get(0) instanceof Common.TextPart) {
            return ((Common.TextPart) this.params.getParts().get(0)).getText();
        }
        throw new RuntimeException("该类型不支持该方法");
    }

    public Map<String, Object> getMetadata() {
        return this.params.getMetadata();
    }
}
