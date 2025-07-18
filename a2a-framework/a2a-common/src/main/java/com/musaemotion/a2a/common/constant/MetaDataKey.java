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

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.constant
 * @project：A2A
 * @date：2025/4/28 16:52
 * @description：请完善描述
 */
public interface MetaDataKey {

	/**
	 * 主任务ID，为了后续扩展
	 */
	String MAIN_TASK_ID = "main_task_id";

	/**
	 * 当前智能体名称
	 */
	String CUR_AGENT_NAME = "agentName";

	/**
	 * 就是 conversation_id 交谈id, 暂时没用，后续其实可以扩展成，也应该是MAIN_TASK_ID
	 */
	String SESSION_ID = "session_id";

	/**
	 * 交谈id, 就是sessionId, 估计是adk里面会读取 conversation_id 字段
	 */
	String CONVERSATION_ID = "conversation_id";

	/**
	 * 输入消息扩展元数据
	 */
	String INPUT_MESSAGE_METADATA = "input_message_metadata";

	/**
	 * 输入消息扩展元数据
	 */
	String INPUT_MESSAGE_ID = "input_message_id";
	/**
	 * 消息id
	 */
	String MESSAGE_ID = "message_id";

	/**
	 * 上一条消息id
	 */
	String LAST_MESSAGE_ID = "last_message_id";

	/**
	 * 当前session 活动 状态
	 */
	String SESSION_ACTIVE = "session_active";


	/**
	 * 完成响应token使用数量
	 */
	String COMPLETION_TOKENS = "completion_tokens";

	/**
	 * 输入内容提示词token使用量
	 */
	String PROMPT_TOKENS = "prompt_tokens";

	/**
	 * 总token使用量
	 */
	String TOTAL_TOKENS = "total_tokens";

	/**
	 * 使用次数
	 */
	String FREQUENCY = "frequency";

	/**
	 * 使用模型
	 */
	String USE_MODEL = "use_model";

	/**
	 * 输入token 金额
	 */
	String PROMPT_TOKENS_AMOUNT = "prompt_tokens_amount";

	/**
	 * 输出token 金额
	 */
	String COMPLETION_TOKENS_AMOUNT = "completion_tokens_amount";

	/**
	 * 总金额
	 */
	String TOTAL_AMOUNT ="total_amount";

}
