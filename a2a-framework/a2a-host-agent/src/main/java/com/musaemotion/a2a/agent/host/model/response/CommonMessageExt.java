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
import com.musaemotion.a2a.common.base.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.model.response
 * @project：A2A
 * @date：2025/5/15 16:29
 * @description：请完善描述
 */
@Data
@EqualsAndHashCode(callSuper =  true)
public class CommonMessageExt extends Common.Message {

    private List<Task> task;

    /**
     *
     * @param message
     * @param task
     * @return
     */
    public static CommonMessageExt fromMessage(Common.Message message, List<Task> task) {
        CommonMessageExt commonMessageExt = new CommonMessageExt();
        BeanUtils.copyProperties(message, commonMessageExt);
        commonMessageExt.setTask(task);
        return commonMessageExt;
    }

    public static CommonMessageExt fromMessage(Common.Message message) {
        CommonMessageExt commonMessageExt = new CommonMessageExt();
        BeanUtils.copyProperties(message, commonMessageExt);
        return commonMessageExt;
    }
}
