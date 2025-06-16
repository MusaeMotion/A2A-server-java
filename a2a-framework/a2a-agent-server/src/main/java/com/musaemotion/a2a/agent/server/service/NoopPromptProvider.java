package com.musaemotion.a2a.agent.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zhangmijia
 * @version 1.0.0
 * @date 2025/6/16  15:02
 * @description 空 Redis服务实现，当项目中Redis依赖时默认实现
 */
@Service
@Slf4j
public class NoopPromptProvider implements PromptProvider {

	private static String prompt ="";

	@Override
	public String getPrompt() {
		return this.prompt;
	}

	@Override
	public Boolean savePrompt(String prompt) {
		this.prompt = prompt;
		return Boolean.TRUE;
	}
}
