package com.a2a.demo.agent.client.dto;

import com.musaemotion.a2a.agent.host.properties.A2aHostAgentProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/17  12:53
 * @description 基座模型模式
 */
@Data
@Builder
public class ChatModelMode {

	/**
	 * 提供者模式
	 */
	private Boolean providerMode;

	/**
	 * 提供者
	 */
	private String provider;

	/**
	 * 配置项
	 */
	private List<A2aHostAgentProperties.ChatModelConfigItem> chatModelConfigs;

	/**
	 * 默认模型的key
	 */
	private String defaultChatModelKey;
}
