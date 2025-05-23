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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.model
 * @project：A2A
 * @date：2025/5/6 09:51
 * @description：请完善描述
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    /**
     * 等于0没有错误，大于0表示错误
     */
    private Integer code;

    /**
     * 一般大于0时不为空
     */
    private String msg;

    /**
     * 数据内容
     */
    private T data;

    /**
     * 构建正确返回
     * @param data
     * @return
     * @param <T>
     */
    public static <T> Result buildSuccess(T data){
        Result<T> result = new Result<T>();
        result.setCode(0);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    /**
     * 构建没有任务返回得正确请求
     * @return
     * @param <T>
     */
    public static <T> Result buildSuccess(){
        Result<T> result = new Result<T>();
        result.setCode(0);
        result.setMsg("success");
        result.setData(null);
        return result;
    }

    /**
     *
     * @param code
     * @param msg
     * @return
     * @param <T>
     */
    public static <T> Result buildError(Integer code, String msg){
        Result<T> result = new Result<T>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}
