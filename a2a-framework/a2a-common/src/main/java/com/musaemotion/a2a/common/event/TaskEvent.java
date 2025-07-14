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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common.event
 * @project：A2A
 * @date：2025/4/23  17:04
 * @description：任务事件基类
 */
@Data
@SuperBuilder
@JsonDeserialize(using = TaskEventDeserializer.class)
@EqualsAndHashCode(callSuper=true)
public abstract class TaskEvent extends AbstractTask {
}
