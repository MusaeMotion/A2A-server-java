package com.a2a.demo.agent.client.service;

import com.a2a.demo.agent.client.entity.SettingEntity;
import com.a2a.demo.agent.client.repository.SettingRepository;
import com.musaemotion.a2a.agent.host.provider.PromptProvider;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.musaemotion.a2a.agent.host.service.HostAgentPromptProvider.ROOT_PROMPT_TPL;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/13  17:49
 * @description 提示词提供者
 */
@Service
@RequiredArgsConstructor
public class HostAgentPromptProvider implements PromptProvider {

	private final String SYSTEM_PROMPT_ID = "SYSTEM_PROMPT";

    private final SettingRepository settingRepository;


	/**
	 * 可以根据你自己的情况通过请求上来的meatdata 构建自己的业务逻辑追加提示词在host agent 的user提示词上
	 * @param sendMessageRequestMetadata
	 * @return
	 */
	@Override
	public String getUserPrompt(Map<String, Object> sendMessageRequestMetadata) {
		return "";
	}

	@Override
	public String getSystemPrompt(Map<String, Object> sendMessageRequestMetadata) {
		return this.settingRepository.findById(SYSTEM_PROMPT_ID).orElse(new SettingEntity()).getValue();
	}

	/**
	 * 设置系统提示词
	 * @param systemPrompt
	 */
	@Transactional(rollbackFor =  Exception.class)
	public void setSystemPrompt(String systemPrompt) {
		SettingEntity settingEntity = new SettingEntity();
		settingEntity.setId(SYSTEM_PROMPT_ID);
		settingEntity.setValue(systemPrompt);
		this.settingRepository.save(settingEntity);
	}

	/**
	 * 获取系统提示词
	 * @return
	 */
	public String getSystemPrompt() {
		String systemPrompt = this.settingRepository.findById(SYSTEM_PROMPT_ID).orElse(new SettingEntity()).getValue();
		if(StringUtils.isEmpty(systemPrompt)){
			systemPrompt = ROOT_PROMPT_TPL;
		}
		return systemPrompt;
	}
}
