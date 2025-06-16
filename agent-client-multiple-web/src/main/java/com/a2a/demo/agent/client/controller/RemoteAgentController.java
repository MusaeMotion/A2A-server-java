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

import com.a2a.demo.agent.client.dto.AgentCardExtend;
import com.a2a.demo.agent.client.dto.SearchRemoteAgent;
import com.a2a.demo.agent.client.service.MysqlRemoteAgentManager;
import com.a2a.demo.agent.client.service.RemoteAgentPromptService;
import com.musaemotion.a2a.agent.host.constant.ControllerSetting;
import com.musaemotion.a2a.agent.host.manager.RemoteAgentRegistryManager;
import com.musaemotion.a2a.agent.host.model.response.PageInfo;
import com.musaemotion.a2a.agent.host.model.response.PageUtils;
import com.musaemotion.a2a.agent.host.model.service.RegisterAgentDto;
import com.musaemotion.a2a.agent.host.model.response.Result;
import com.musaemotion.a2a.common.AgentCard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	/**
	 * 远程智能体注册中心
	 */
	private final RemoteAgentRegistryManager remoteAgentRegistry;

	/**
	 * 远程智能体管理器
	 */
	private final MysqlRemoteAgentManager remoteAgentManager;

	/**
	 * 远程提示词服务
	 */
	private final RemoteAgentPromptService remoteAgentPromptService;


    /**
     * 注册智能体
     * @param input
     * @return
     */
    @PostMapping
    public ResponseEntity<AgentCard> registerAgent(@RequestBody RegisterAgentDto input) {
		// remoteAgentRegistry.registerAgent 注册之后会调用 remoteAgentManager 实现的 save 方法
		// remoteAgentRegistry.registerAgent 1.会注册远程智能体的连接 2.同时注册到通知服务，远程智能体发送通知时会鉴权
        return ResponseEntity.ok(this.remoteAgentRegistry.registerAgent(input));
    }

    /**
     * 智能体列表
     * @param searchInput
     * @param pageNum
     * @param pageSize
     * @return
     */
    @PostMapping("/list/{pageNum}/{pageSize}")
    public ResponseEntity pageList(@RequestBody SearchRemoteAgent searchInput, @PathVariable Integer pageNum, @PathVariable Integer pageSize) {
		pageNum = pageNum - 1;
		Page<AgentCardExtend> page = this.remoteAgentManager.pageList(searchInput, pageNum, pageSize);
		PageInfo<AgentCardExtend> pageInfo = PageUtils.springPageToMyPage(page);
		// 包装提示词
		pageInfo.setList(
				pageInfo.getList().stream()
						.map(agentCardExtend -> agentCardExtend.buildPrompt((agentName)->this.remoteAgentPromptService.getRemoteAgentPrompt(agentName)))
						.collect(Collectors.toUnmodifiableList())
		);
        return ResponseEntity.ok(Result.buildSuccess(
				pageInfo
        ));
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public ResponseEntity delete(@RequestParam("id") String ids) {
		if (StringUtils.hasText(ids)) {
			List<String> id = Arrays.stream(ids.split(",")).collect(Collectors.toList());
			this.remoteAgentManager.delete(id);
		}
        return ResponseEntity.ok(Result.buildSuccess());
    }

	/**
	 * 切换状态
	 * @param id
	 * @return
	 */
	@PutMapping("/enable/{id}")
	public ResponseEntity changeAgentEnable(@PathVariable String id) {
		this.remoteAgentManager.changeAgentEnable(id);
		return ResponseEntity.ok(Result.buildSuccess());
	}

	/**
	 * 刷新智能体，也等于重新注册
	 * @param id
	 * @return
	 */
	@PutMapping("/re-register/{id}")
	public ResponseEntity reRegister(@PathVariable String id) {
		Optional<AgentCardExtend> optional = this.remoteAgentManager.getById(id);
		if (optional.isPresent()) {
			this.remoteAgentManager.registerAgent(optional.get().getUrl());
		}
		return ResponseEntity.ok(Result.buildSuccess());
	}

	/**
	 * 注册智能体
	 * @param input
	 * @return
	 */
	@PostMapping("/{id}")
	public ResponseEntity updateAgent(@RequestBody AgentCardExtend input, @PathVariable String id) {
		if(input.getCapabilities().modifyPrompt()){
           this.remoteAgentPromptService.saveRemoteAgentPrompt(input.getName(), input.getAgentPrompt());
		}
		AgentCard agentCard = input.toAgentCard();
		this.remoteAgentManager.updateAgentCard(id, agentCard);

		return ResponseEntity.ok(Result.buildSuccess());
	}
}
