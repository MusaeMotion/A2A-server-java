package com.musaemotion.a2a.agent.server.service;


/**
 * @author zhangmijia
 * @version 1.0.0
 * @date 2025/6/16  15:02
 * @description  Redis服务接口，定义了基本的查询功能
 */
public interface PromptProvider {

	/**
	 * 获取提示词
	 * @return
	 */
	String getPrompt();

	/**
	 * 保存提示词
	 * @param prompt
	 * @return
	 */
	Boolean savePrompt(String prompt);
}
