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

package com.musaemotion.a2a.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.musaemotion.a2a.common.base.Common;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common.event
 * @project：A2A
 * @date：2025/4/23 14:49
 * @description：请完善描述
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TaskStatusUpdateEvent extends TaskEvent {

    /**
     * 状态
     */
    private Common.TaskStatus status;

    /**
     * 是否完成
     */
    @JsonProperty("final")
    private Boolean done;



}
