package com.musaemotion.a2a.agent.server.mcp.core;

import com.musaemotion.a2a.agent.server.mcp.manager.ToolManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.stereotype.Component;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/23  16:05
 * @description
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DiscoverToolCallbackResolver implements ToolCallbackResolver {
	/**
	 * 动态工具服务
	 */
	private final ToolManager toolManager;

	/**
	 * 执行工具
	 * @param toolName
	 * @return
	 */
	@Override
	public ToolCallback resolve(String toolName) {
		log.warn("DiscoverToolCallbackResolver {}, ", toolName);
		var op = toolManager.findToolByName(toolName);
		if (op.isPresent()) {
			return op.get();
		}
		return null;
	}
}
