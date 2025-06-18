package com.musaemotion.a2a.agent.host.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/18  14:38
 * @description 默认实现者
 */
@Slf4j
public class DefaultChatModelProviderImpl implements ChatModelProvider {

	private ChatModel chatModel;

	public DefaultChatModelProviderImpl(ChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@Override
	public ChatModel getChatModel() {
		log.info("DefaultChatModelProviderImpl");
		return this.chatModel;
	}
}
