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

package com.musaemotion.agent;

import java.util.Map;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.framework
 * @project：A2A
 * @date：2025/4/28 13:26
 * @description：主机智能体提示词
 */
public interface AgentPromptProvider {

	/**
	 * 请求消息的metadata
	 * @param sendMessageRequestMetadata
	 * @return
	 */
	String userPrompt(Map<String, Object>  sendMessageRequestMetadata);

	/**
	 * 系统提示词
	 * @param toolContext 工具上下文对象
	 * @param sendMessageRequestMetadata 其他传递的上下文对象
	 * @return
	 */
	String systemPrompt(Map<String, Object> toolContext, Map<String, Object>  sendMessageRequestMetadata);

}
