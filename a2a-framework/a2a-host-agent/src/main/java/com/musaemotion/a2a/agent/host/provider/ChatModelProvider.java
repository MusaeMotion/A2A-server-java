package com.musaemotion.a2a.agent.host.provider;

import org.springframework.ai.chat.model.ChatModel;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/16  17:27
 * @description host agent 基座模型提供者
 */

public interface  ChatModelProvider {


	/**
	 * 获取默认设置模型
	 * @return
	 */
	 ChatModel getDefaultChatModel();

	/**
	 * 设置默认模型，这里使用key, 你可以用id,或者名称
	 * @param key
	 */
	ChatModel setDefaultChatModelKey(String key);

	/**
	 * 获取默认模型的key
	 * @return
	 */
	String getDefaultChatModelKey();

}
