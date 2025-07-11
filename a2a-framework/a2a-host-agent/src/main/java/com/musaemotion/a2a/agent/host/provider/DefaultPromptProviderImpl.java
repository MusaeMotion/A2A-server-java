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

package com.musaemotion.a2a.agent.host.provider;

import com.musaemotion.agent.AgentPromptProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.musaemotion.a2a.common.constant.MetaDataKey.CUR_AGENT_NAME;
import static com.musaemotion.a2a.common.constant.MetaDataKey.SESSION_ACTIVE;
import static com.musaemotion.agent.BasisAgent.STATE;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.service
 * @project：A2A
 * @date：2025/5/15 11:31
 * @description：请完善描述
 */
@Service
@Slf4j
public class DefaultPromptProviderImpl implements AgentPromptProvider {

	/**
	 * 自定义提示词提供者
	 */
	private PromptProvider promptProvider;

	/**
	 * check_pending_task_states 有可能在框架adk内部实现的，因为 adk 会创建一个任务任务列表出来。
	 */
	public static String ROOT_PROMPT_TPL = """
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
			
				 * **Task Delegation:** Utilize the `sendTask` function to assign actionable tasks to remote agents.
				 * **Contextual Awareness for Remote Agents:** If a remote agent repeatedly requests user confirmation, assume it lacks access to the         full conversation history. In such cases, enrich the task description with all necessary contextual information relevant to that         specific agent.
				 * **Autonomous Agent Engagement:** Never seek user permission before engaging with remote agents. If multiple agents are required to         fulfill a request, connect with them directly without requesting user preference or confirmation.
				 * **Transparent Communication:** Always present the complete and detailed response from the remote agent to the user.
				 * **User Confirmation Relay:** If a remote agent asks for confirmation, and the user has not already provided it, relay this         confirmation request to the user.
				 * **Focused Information Sharing:** Provide remote agents with only relevant contextual information. Avoid extraneous details.
				 * **No Redundant Confirmations:** Do not ask remote agents for confirmation of information or actions.
				 * **Tool Reliance:** Strictly rely on available tools to address user requests. Do not generate responses based on assumptions. If         information is insufficient, request clarification from the user.
				 * **Prioritize Recent Interaction:** Focus primarily on the most recent parts of the conversation when processing requests.
				 * **Active Agent Prioritization:** If an active agent is already engaged, route subsequent related requests to that agent using the         appropriate task update tool.
		
			      Current agent: %s
			""";


	/**
	 * @param promptProvider 用户自定义提示词处理器
	 */
	@Autowired
	public DefaultPromptProviderImpl(@Autowired(required = false) PromptProvider promptProvider) {
		this.promptProvider = promptProvider;
	}

	/**
	 * 构建消息
	 * @param sendMessageRequestMetadata
	 * @return
	 */
	@Override
	public String userPrompt(Map<String, Object> sendMessageRequestMetadata) {
		if (this.promptProvider == null) {
			return "";
		}
		return this.promptProvider.getUserPrompt(sendMessageRequestMetadata);
	}

	/**
	 * 系统提示词
	 * @param toolContext 工具上下文对象
	 * @param sendMessageRequestMetadata 请求消息的metadata
	 * @return
	 */
	@Override
	public String systemPrompt(Map<String, Object> toolContext, Map<String, Object> sendMessageRequestMetadata) {
		String systemPrompt = "";
		if (this.promptProvider != null) {
			systemPrompt = this.promptProvider.getSystemPrompt(sendMessageRequestMetadata);
		}
		if (StringUtils.isEmpty(systemPrompt)) {
			systemPrompt = String.format(ROOT_PROMPT_TPL, getActiveAgent((Map<String, Object>) toolContext.get(STATE)));
		}
		return systemPrompt;
	}


	/**
	 * 获取当前智能体激活状态
	 * @param state
	 * @return
	 */
	private String getActiveAgent(Map<String, Object> state) {
		// 如果当前活动状态为true 表示有智能体在运行，获取当前智能体名称
		if (state.containsKey(SESSION_ACTIVE)
				&& (Boolean) (state.get(SESSION_ACTIVE))
				&& state.containsKey(CUR_AGENT_NAME)) {
			return state.get(CUR_AGENT_NAME).toString();
		}
		return "None";
	}

}
