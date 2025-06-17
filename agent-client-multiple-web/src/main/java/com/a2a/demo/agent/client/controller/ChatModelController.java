package com.a2a.demo.agent.client.controller;

import com.a2a.demo.agent.client.dto.ChatModelMode;
import com.musaemotion.a2a.agent.host.constant.ControllerSetting;
import com.musaemotion.a2a.agent.host.properties.A2aHostAgentProperties;
import com.musaemotion.a2a.agent.host.provider.ChatModelProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/17  12:39
 * @description 基座模型更换配置
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping(ControllerSetting.CHAT_MODEL)
@RestController
public class ChatModelController {


	private A2aHostAgentProperties a2aHostAgentProperties;

	private ChatModelProvider chatModelProvider;

	@Autowired
	public ChatModelController(A2aHostAgentProperties a2aHostAgentProperties, @Autowired(required = false) ChatModelProvider chatModelProvider){
         this.chatModelProvider = chatModelProvider;
		 this.a2aHostAgentProperties = a2aHostAgentProperties;
	}

	/**
	 * 获取模型配置
	 * @return
	 */
	@GetMapping("/mode")
	public ResponseEntity getChatModelMode() {
		var builder = ChatModelMode.builder().providerMode(this.a2aHostAgentProperties.getChatModelProvider());
		if (this.a2aHostAgentProperties.getChatModelProvider() != null &&
				this.a2aHostAgentProperties.getChatModelProvider() &&
				this.chatModelProvider != null) {
			builder.provider(this.chatModelProvider.getClass().getName());
			builder.chatModelConfigs(this.a2aHostAgentProperties.getChatModelConfigs());
			builder.defaultChatModelKey(this.chatModelProvider.getDefaultChatModelKey());
		}
		return ResponseEntity.ok(builder.build());
	}

	/**
	 * 设置默认模型
	 * @param key
	 * @return
	 */
	@PutMapping("/set-chat-model/{key}")
	public ResponseEntity setChatModelMode(@PathVariable("key") String key) {
		this.chatModelProvider.setDefaultChatModelKey(key);
		return ResponseEntity.ok("");
	}

}
