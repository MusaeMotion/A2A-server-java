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

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：
 */
public enum TaskState {

    //提交
    SUBMITTED("submitted"),

    // 工作中
    WORKING("working"),

    // 需要输入
    INPUT_REQUIRED("input-required"),

    // 完成
    COMPLETED("completed"),

    // 取消
    CANCELED("canceled"),

    // 失败
    FAILED("failed"),

    // 未知
    UNKNOWN("unknown");

    private String value;

    TaskState(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonValue
    public void setValue(String input) {
        this.value = input;
    }

    @Override
    public String toString() {
        return this.value;
    }
    // 通过字符串获取枚举值
    public static TaskState fromString(String state) {
        for (TaskState value : TaskState.values()) {
            if (value.value.equalsIgnoreCase(state)) {
                return value;
            }
        }
        return UNKNOWN; // 如果找不到匹配项，返回 UNKNOWN
    }
}
