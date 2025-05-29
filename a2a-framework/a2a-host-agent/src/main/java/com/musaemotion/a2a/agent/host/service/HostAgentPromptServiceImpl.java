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

package com.musaemotion.a2a.agent.host.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.agent.host.manager.AbstractRemoteAgentManager;
import com.musaemotion.a2a.agent.host.model.RemoteAgentInfo;
import com.musaemotion.agent.HostAgentPromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

import static com.musaemotion.a2a.common.constant.MetaDataKey.*;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.service
 * @project：A2A
 * @date：2025/5/15 11:31
 * @description：请完善描述
 */
@Service
@Slf4j
public class HostAgentPromptServiceImpl implements HostAgentPromptService {

	private static AbstractRemoteAgentManager remoteAgentManager;

	/**
	 * check_pending_task_states 有可能在框架adk内部实现的，因为 adk 会创建一个任务任务列表出来。
	 */
	private String ROOT_PROMPT_TPL =  """
        You are a expert delegator that can delegate the user request to the
        appropriate remote agents.
        
        Discovery:
        - You can use `listRemoteAgents` to list the available remote agents you
        can use to delegate the task.
        
        Execution:
        - For actionable tasks, you can use `sendTask` to assign tasks to remote agents to perform.
        Be sure to include the remote agent name when you response to the user.
        
        Please rely on tools to address the request, don't make up the response. If you are not sure, please ask the user for more details.
        Focus on the most recent parts of the conversation primarily.
        
        If there is an active agent, send the request to that agent with the update task tool.
        
        Please do not create your own tools
        
        Agents:
        %s
        
        Current agent: %s
        """;


	/**
	 * 远程智能体管理器
	 * @param remoteAgentManager
	 */
	@Autowired
	public HostAgentPromptServiceImpl(AbstractRemoteAgentManager remoteAgentManager) {
		this.remoteAgentManager = remoteAgentManager;
	}


	@Override
	public String hostAgentSystemPrompt(Map<String, Object> state) {
		String systemPrompt = String.format(ROOT_PROMPT_TPL, this.loadRemoteAgentsToString(), getActiveAgent(state));
		log.error("hostAgentSystemPrompt：{}", systemPrompt);
		return systemPrompt;
	}


	/**
	 * 获取当前激活状态的智能体， 也就是再后面调用中，不会再调用到方法，在工具内部则调用了。
	 * @param state
	 * @return
	 */
	private String getActiveAgent(Map<String, Object> state) {
		// 对话存在，并且智能体激活状态，包含了智能体名称，则返回智能体， 激活状态为true, 同时当前智能体也存在
		if (state.containsKey(SESSION_ID)
				&& state.containsKey(SESSION_ACTIVE)
				&& (Boolean) (state.get(SESSION_ACTIVE))
				&& state.containsKey(CUR_AGENT_NAME)) {
			return state.get(CUR_AGENT_NAME).toString();
		}
		return "None";
	}

	/**
	 * 获取当前智能体列表
	 * @return
	 */
	private List<RemoteAgentInfo> loadRemoteAgents() {
		var agents = this.remoteAgentManager.listAll();
		if (CollectionUtils.isEmpty(agents)) {
			return Lists.newArrayList();
		}
		List<RemoteAgentInfo> remoteAgentInfos = Lists.newArrayList();
		agents.forEach(agentCard -> {
			remoteAgentInfos.add(
					RemoteAgentInfo.builder().name(agentCard.getName())
							.description(agentCard.getDescription())
							.skills(agentCard.getSkills())
							.build()
			);
		});
		return remoteAgentInfos;
	}

	/**
	 * 获取字符串列表
	 * @return
	 */
	public String loadRemoteAgentsToString() {
		StringBuffer sb = new StringBuffer();
		ObjectMapper mapper = new ObjectMapper();
		this.loadRemoteAgents().forEach(agentInfo -> {
			try {
				sb.append(mapper.writeValueAsString(agentInfo)+"\n");
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
		return sb.toString();
	}
}
