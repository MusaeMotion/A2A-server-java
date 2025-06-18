package com.musaemotion.a2a.agent.host.provider;

import org.springframework.ai.chat.model.ChatModel;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/16  17:27
 * @description host agent 基座模型提供者
 */

public interface ChatModelProvider {

	/**
	 * 获取当前默认模型
	 * @return
	 */
	ChatModel getChatModel();

}
