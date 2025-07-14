package com.musaemotion.a2a.agent.host.provider;

import com.musaemotion.a2a.agent.host.properties.A2aHostAgentProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/16  17:49
 * @description yaml文件实现基座智能体切换实现（只支持标准的open ai规范的模型）
 */
@Slf4j
public class MemoryYamlChatModelProviderImpl implements ChatModelProvider {


	private A2aHostAgentProperties a2aHostAgentProperties;

	private ChatModel defaultChatModel;

	private String defaultChatModelKey;

	/**
	 * @param a2aHostAgentProperties
	 */
	public MemoryYamlChatModelProviderImpl(A2aHostAgentProperties a2aHostAgentProperties) {
		this.a2aHostAgentProperties = a2aHostAgentProperties;
	}


	/**
	 * 配置chatModel模型
	 * @param config
	 * @return
	 */
	private ChatModel buildChatModel(A2aHostAgentProperties.ChatModelConfigItem config) {
		var openApi = OpenAiApi.builder()
				.apiKey(config.apiKey())
				.baseUrl(config.baseUrl())
				.build();
		var openAiChatOptions = OpenAiChatOptions.builder()
				.model(config.model())
				.temperature(config.temperature() == null ? 0.7 : config.temperature())
				// 启动stream 使用
				.streamUsage(Boolean.TRUE)
				.build();

		return  OpenAiChatModel.builder().openAiApi(openApi).defaultOptions(openAiChatOptions).build();
	}



	/**
	 * 获取默认聊天模型
	 * @return
	 */
	@Override
	public ChatModel getChatModel()  {

		if(defaultChatModel == null) {
			return this.setDefaultChatModelKey(this.a2aHostAgentProperties.getChatModelConfigs().get(0).name());
		}
		// log.info("MemoryYamlChatModelProviderImpl:{}", this.defaultChatModelKey);
		return this.defaultChatModel;
	}

	/**
	 * 设置当前的模型的key
	 * @param key
	 * @return
	 */
	public ChatModel setDefaultChatModelKey(String key) {
		var optional = a2aHostAgentProperties.getChatModelConfigs().stream().filter(item -> item.name().equals(key)).findFirst();
		if (optional.isPresent()) {
			this.defaultChatModel = buildChatModel(optional.get());
			this.defaultChatModelKey = key;
			return this.defaultChatModel;
		}
		log.error("未找到 {} 该模型配置", key);
		return null;
	}

	/**
	 * 获取当前模型的key
	 * @return
	 */
	public String getDefaultChatModelKey()  {
		if (defaultChatModel == null) {
			this.setDefaultChatModelKey(this.a2aHostAgentProperties.getChatModelConfigs().get(0).name());
			return this.defaultChatModelKey;
		}
		return this.defaultChatModelKey;
	}


}
