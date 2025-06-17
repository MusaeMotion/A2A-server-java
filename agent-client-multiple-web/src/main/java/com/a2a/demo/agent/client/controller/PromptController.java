package com.a2a.demo.agent.client.controller;

import com.a2a.demo.agent.client.service.HostAgentPromptProvider;
import com.musaemotion.a2a.agent.host.constant.ControllerSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/13  18:08
 * @description 设置配置
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping(ControllerSetting.PROMPT)
@RestController
public class PromptController {

	/**
	 * 系统
	 */
	private final HostAgentPromptProvider hostAgentPromptProvider;


	/**
	 * 注册智能体
	 * @param input
	 * @return
	 */
	@PostMapping("/host-agent/system-prompt")
	public ResponseEntity saveHostAgentSystemPrompt(@RequestBody String input) {
		this.hostAgentPromptProvider.setSystemPrompt(input);
		return ResponseEntity.ok("");
	}

	/**
	 * 获取系统提示词
	 * @return
	 */
	@GetMapping("/host-agent/system-prompt")
	public ResponseEntity getHostAgentSystemPrompt() {
		return ResponseEntity.ok(this.hostAgentPromptProvider.getSystemPrompt());
	}


	
}
