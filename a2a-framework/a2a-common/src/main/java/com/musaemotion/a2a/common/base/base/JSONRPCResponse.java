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

package com.musaemotion.a2a.common.base.base;

import com.musaemotion.a2a.common.base.error.TaskNotFoundError;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：响应基类
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class JSONRPCResponse<T> extends JSONRPCMessage {

    protected T result;

    protected JSONRPCError error;


    /**
     * 直接构造错误
     * @param id
     * @param jsonrpcError
     * @return
     */
    public static JSONRPCResponse  buildError(String id, JSONRPCError jsonrpcError) {
        JSONRPCResponse response = new JSONRPCResponse();
        return buildError(response, id, jsonrpcError);
    }
    /**
     * 构建错误
     * @param response
     * @param id
     * @param jsonrpcError
     * @return
     */
    protected static <D extends JSONRPCResponse> D  buildError(D response, String id, JSONRPCError jsonrpcError) {
        response.setError(jsonrpcError);
        response.setId(id);
        return response;
    }

    /**
     * 构建未找到任务错误
     * @param response
     * @param id
     * @return
     */
    protected static <D extends JSONRPCResponse> D buildTaskNotFoundError(D response, String id) {
        return buildError(response, id, new TaskNotFoundError());
    }
}
