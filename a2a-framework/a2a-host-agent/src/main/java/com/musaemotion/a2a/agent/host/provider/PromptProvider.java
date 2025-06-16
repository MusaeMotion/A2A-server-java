package com.musaemotion.a2a.agent.host.provider;

import java.util.Map;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/4  17:10
 * @description 用户提示词提供者
 */
public interface PromptProvider {

	/**
	 * 用户提示词提供者
	 * @param sendMessageRequestMetadata
	 * @return
	 */
	String getUserPrompt(Map<String, Object> sendMessageRequestMetadata);

	/**
	 * 系统提示词
	 * @param sendMessageRequestMetadata
	 * @return
	 */
	String getSystemPrompt(Map<String, Object> sendMessageRequestMetadata);
}
