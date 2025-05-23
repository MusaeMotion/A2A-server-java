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

package com.musaemotion.a2a.agent.host.controller;

import com.musaemotion.a2a.agent.host.constant.ControllerSetting;
import com.musaemotion.a2a.agent.host.model.service.RegisterAgentDto;
import com.musaemotion.a2a.agent.host.model.response.Result;
import com.musaemotion.a2a.common.AgentCard;
import com.musaemotion.a2a.agent.host.model.service.SearchRemoteAgentDto;
import com.musaemotion.a2a.agent.host.manager.HostAgentManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.controller
 * @project：A2A
 * @date：2025/4/30 11:54
 * @description：请完善描述
 */
@RestController
@Slf4j
@RequestMapping(ControllerSetting.REMOTE_AGENT)
@RequiredArgsConstructor
public class RemoteAgentController {


    private final HostAgentManager hostAgentManager;

    /**
     * 注册智能体
     * @param input
     * @return
     */
    @PostMapping
    public ResponseEntity<AgentCard> registerAgent(@RequestBody RegisterAgentDto input) {
        return ResponseEntity.ok(this.hostAgentManager.registerAgent(input));
    }

    /**
     * 智能体列表
     * @param searchInput
     * @param pageNum
     * @param pageSize
     * @return
     */
    @PostMapping("/list/{pageNum}/{pageSize}")
    public ResponseEntity pageList(@RequestBody SearchRemoteAgentDto searchInput, @PathVariable Integer pageNum, @PathVariable Integer pageSize) {
        return ResponseEntity.ok(Result.buildSuccess(
                this.hostAgentManager.pageListAgent(searchInput, pageNum, pageSize)
        ));
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public ResponseEntity delete(@RequestParam("id") String ids) {
        this.hostAgentManager.deleteAgent(ids);
        return ResponseEntity.ok(Result.buildSuccess());
    }

}
