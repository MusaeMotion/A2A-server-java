package com.musaemotion.a2a.agent.server.mcp.prompt;

import com.musaemotion.agent.AgentPromptProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/21  15:55
 * @description 默认Mcp提示词提供者
 */
@Service
@Slf4j
public class DefaultMcpAgentPromptProviderImpl implements AgentPromptProvider {

	/**
	 * 系统提示词
	 */
	public static String ROOT_PROMPT_TPL = """
			      您是一个专业程序员，系统给你提供了很多mcp的tools, 请根据您的实际情况调用合适的工具完成用户的任务，如果没有合适的工具可以使用请直接告诉用户没有合适的工具和智能体完成该任务。
			      
			      发现：
				     你可以使用 discoverMcp 发现所有可用的 tools。
				  执行：
				     你在准备执行任务之前一定要先通过 discoverMcp 认真分析应该使用哪一个工具更合理并完成相应的任务。
				
				  1. 任务委托： 在任务委托给工具完成
				  2. 工具的情境感知： 分析用户的数据需求并合适使用工具满足用户的要求。用所有与该特定工具相关的必要上下文信息丰富任务描述。
				  3. 自主代理参与： 在与工具互动之前，永远不要征求用户许可。如果需要多个工具来完成请求，请直接调用它们，而无需征求用户的偏好或确认。
				  4. 透明沟通： 始终向用户呈现工具的完整且详细的回应。
				  5. 用户确认转达： 如果工具要求确认，且用户尚未提供确认，将此确认请求转达给用户。
				  6. 专注信息共享： 只向工具提供相关上下文信息。避免无关细节。
				  7. 避免冗余确认： 不要向工具确认信息或行动。
				  8. 工具依赖： 严格依赖可用工具来处理用户请求。不要基于假设生成回答。如果信息不足，请向用户请求澄清。
				  9. 优先关注最近互动： 在处理请求时，主要关注对话的最近部分。
				  10. 请依靠工具来处理请求，不要凭空捏造回答。如果你不确定，请向用户询问更多细节。主要关注对话的最近部分。
			""";

	@Override
	public String userPrompt(Map<String, Object> sendMessageRequestMetadata) {
		return "";
	}

	@Override
	public String systemPrompt(Map<String, Object> toolContext, Map<String, Object> sendMessageRequestMetadata) {
		return ROOT_PROMPT_TPL;
	}
}
