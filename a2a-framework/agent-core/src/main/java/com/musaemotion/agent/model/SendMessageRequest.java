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
import java.util.stream.Collectors;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.framework.model
 * @project：A2A
 * @date：2025/4/28 13:18
 * @description：请完善描述
 */
@Data
public class SendMessageRequest {

	/**
	 * 消息体
	 */
	private Common.Message params;

	/**
	 * 交谈id
	 * @return
	 */
    public String getConversationId() {
        return this.params.getConversationId();
    }

	/**
	 * 消息id
	 * @return
	 */
    public String getMessageId() {
        return this.params.getMessageId();
    }

	/**
	 * 上一次消息id
	 * @return
	 */
    public String getLastMessageId() {
        return this.params.getLastMessageId();
    }

	/**
	 * 获取消息文本内容
	 * @return
	 */
	public String getContent() {
		var textParts =  this.params.getParts().stream().filter(part -> part instanceof Common.TextPart).collect(Collectors.toUnmodifiableList());
		if(textParts.isEmpty()) {
			throw new RuntimeException("该类型不支持该方法");
		}
		// 默认取第一条文本消息作为上下文处理消息
		return ((Common.TextPart) textParts.get(0)).getText();
	}

	/**
	 * mataData
	 * @return
	 */
    public Map<String, Object> getMetadata() {
        return this.params.getMetadata();
    }
}
