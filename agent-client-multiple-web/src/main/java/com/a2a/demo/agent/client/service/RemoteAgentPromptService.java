package com.a2a.demo.agent.client.service;

import com.musaemotion.a2a.common.constant.RemoteAgentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/16  11:24
 * @description redis操作
 */
@Service
public class RemoteAgentPromptService {

	private StringRedisTemplate stringRedisTemplate;

	/**
	 *
	 * @param stringRedisTemplate
	 */
	@Autowired
	public RemoteAgentPromptService(StringRedisTemplate stringRedisTemplate) {
		this.stringRedisTemplate = stringRedisTemplate;
	}

	/**
	 * 获取远程智能体提示词
	 * @param agentName
	 * @return
	 */
	public String getRemoteAgentPrompt(String agentName) {
		String key = String.format(RemoteAgentConstants.REDIS_PROMPT_PREFIX_TPL, agentName);
		return stringRedisTemplate.opsForValue().get(key);
	}

	/**
	 * 保存远程智能提示词
	 * @param agentName
	 * @param prompt
	 */
	public void saveRemoteAgentPrompt(String agentName, String prompt) {
		String key = String.format(RemoteAgentConstants.REDIS_PROMPT_PREFIX_TPL, agentName);
		if(prompt == null){
			prompt = "";
		}
		stringRedisTemplate.opsForValue().set(key, prompt);
	}


}
