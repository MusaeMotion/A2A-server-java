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

package com.a2a.demo.agent.client.controller;

import com.a2a.demo.agent.client.service.MysqlTaskCenterManager;
import com.musaemotion.a2a.agent.host.constant.ControllerSetting;
import com.musaemotion.a2a.agent.host.model.response.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.controller
 * @project：A2A
 * @date：2025/5/12 16:44
 * @description：请完善描述
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(ControllerSetting.TASK)
public class TaskController {

	/**
	 * 智能体任务管理器
	 */
	private final MysqlTaskCenterManager taskCenterManager;

    /**
     * 根据对话id获取列表
     * @param sessionId
     * @return
     */
    @GetMapping
    public ResponseEntity taskList(@RequestParam(value = "sessionId", required = false) String sessionId) {
        return ResponseEntity.ok(Result.buildSuccess(this.taskCenterManager.getByConversationId(sessionId)));
    }

    /**
     * 根据taskId删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public ResponseEntity deleteTask(@RequestParam("id") String ids) {
		if (StringUtils.hasText(ids)) {
			List<String> id = Arrays.stream(ids.split(",")).collect(Collectors.toList());
			this.taskCenterManager.removeTask(id);
		}
        return ResponseEntity.ok(Result.buildSuccess());
    }
}
