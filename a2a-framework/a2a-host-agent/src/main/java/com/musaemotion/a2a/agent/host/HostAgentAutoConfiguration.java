/*
 * Copyright (c) 2025 MusaeMotion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.musaemotion.a2a.agent.host;


import com.musaemotion.a2a.agent.client.INotificationConsumer;
import com.musaemotion.a2a.agent.client.server.PushNotificationServer;
import com.musaemotion.a2a.agent.host.ext.A2AToolCallingManager;
import com.musaemotion.a2a.agent.host.ext.MyToolExecutionExceptionProcessor;
import com.musaemotion.a2a.agent.host.properties.A2aHostAgentProperties;
import com.musaemotion.a2a.agent.host.provider.ChatModelProvider;
import com.musaemotion.a2a.agent.host.provider.DefaultChatModelProviderImpl;
import com.musaemotion.a2a.agent.host.provider.MemoryYamlChatModelProviderImpl;
import com.musaemotion.agent.AgentPromptProvider;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@Component
@EnableConfigurationProperties({
        A2aHostAgentProperties.class
})
@ComponentScan("com.musaemotion.a2a.agent.host")
@RequiredArgsConstructor
public class HostAgentAutoConfiguration {


    /**
     * 工具调用管理器
     * @return
     */
    @Bean(value = "toolCallingManager")
    public A2AToolCallingManager toolCallingManager(AgentPromptProvider agentPromptProvider, @Autowired(required = false) ObservationRegistry observationRegistry) {
        return A2AToolCallingManager.builder()
                .toolExecutionExceptionProcessor(new MyToolExecutionExceptionProcessor())
                .observationRegistry(observationRegistry == null ? ObservationRegistry.NOOP : observationRegistry)
                .hostAgentPromptService(agentPromptProvider)
                .build();
    }

    /**
     * 通知服务
     * @return
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    @Bean("pushNotificationServer")
    @ConditionalOnProperty(name = "musaemotion.a2a.host-agent.notify-url")
    public PushNotificationServer pushNotificationServer(@Autowired A2aHostAgentProperties a2aHostAgentProperties, @Autowired List<INotificationConsumer> notificationConsumers) throws URISyntaxException, UnknownHostException {
        URI uri = new URI(a2aHostAgentProperties.getNotifyUrl());
        PushNotificationServer pushNotificationServer = new PushNotificationServer(
                InetAddress.getByName(uri.getHost()),
                uri.getPort(),
				// 默认通知消费者，会调用 ApplicationEventPublisher 消息 ,demo host-agent项目 ChatController 有处理该消息的示例，推送给前端。
				notificationConsumers,
				a2aHostAgentProperties.getExternalUrl()
        );
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(pushNotificationServer::strat);
        return pushNotificationServer;
    }

	/**
	 * 聊天内容
	 * @return
	 */
	@Bean
	public ChatMemoryRepository chatMemoryRepository() {
	   return new InMemoryChatMemoryRepository();
	}


	/**
	 * 您可以创建您自己的实现，默认的 MemoryYamlChatModelProvider 只支持基与open ai标准接口的模型
	 * @param properties
	 * @return
	 */
	@Bean
	@ConditionalOnProperty(prefix = "musaemotion.a2a.host-agent", name = "chat-model-provider", havingValue = "true")
	public ChatModelProvider memoryYamlChatModelProvider(A2aHostAgentProperties properties){
		return new MemoryYamlChatModelProviderImpl(properties);
	}

	/**
	 * 未配置构造默认模型提供者
	 * @param chatModel
	 * @return
	 */
	@Bean
	@ConditionalOnProperty(prefix = "musaemotion.a2a.host-agent", name = "chat-model-provider", havingValue = "false", matchIfMissing = true)
	public ChatModelProvider defaultChatModelProvider(ChatModel chatModel) {
		return new DefaultChatModelProviderImpl(chatModel);
	}

}
