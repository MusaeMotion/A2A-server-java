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

package com.musaemotion.a2a.common.base.error;

import com.musaemotion.a2a.common.base.base.JSONRPCError;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common.common.error
 * @project：A2A
 * @date：2025/4/23 10:33
 * @description：请完善描述
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InvalidParamsError extends JSONRPCError {

    /**
     * 构造函数
     */
    public InvalidParamsError() {
        this(-32602, "Invalid parameters");
    }

    /**
     * 构造函数
     */
    public InvalidParamsError(String message) {
        this(-32602, message);
    }

    /**
     * 构造函数
     */
    public InvalidParamsError(Integer code, String message) {
        this.setCode(code);
        this.setMessage(message);
    }

}
