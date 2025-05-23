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

package com.musaemotion.a2a.agent.host.model.response;

import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.base.JSONRPCResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common.response
 * @project：A2A
 * @date：2025/4/24 14:59
 * @description：交谈客户端 用到
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class SendMessageResponse<T> extends JSONRPCResponse<T> {

    /**
     * 创建
     * @param t
     * @return
     */
    public static  SendMessageResponse buildMessageResponse(Common.Message t, String conversationId) {
        SendMessageResponse response = new SendMessageResponse();
        response.setResult(t);
        response.setId(conversationId);
        return response;
    }
}
