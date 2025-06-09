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
import com.musaemotion.a2a.agent.host.ext.A2AToolCallingManager;
import com.musaemotion.a2a.agent.host.ext.MyToolExecutionExceptionProcessor;
import com.musaemotion.a2a.agent.host.properties.A2aHostAgentProperties;
import com.musaemotion.a2a.agent.client.server.PushNotificationServer;
import com.musaemotion.agent.HostAgentPromptService;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public A2AToolCallingManager toolCallingManager(HostAgentPromptService hostAgentPromptService, @Autowired(required = false) ObservationRegistry observationRegistry) {
        return A2AToolCallingManager.builder()
                .toolExecutionExceptionProcessor(new MyToolExecutionExceptionProcessor())
                .observationRegistry(observationRegistry == null ? ObservationRegistry.NOOP : observationRegistry)
                .hostAgentPromptService(hostAgentPromptService)
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
    public PushNotificationServer pushNotificationServer(@Autowired A2aHostAgentProperties a2aHostAgentProperties, @Autowired INotificationConsumer notificationConsumer) throws URISyntaxException, UnknownHostException {
        URI uri = new URI(a2aHostAgentProperties.getNotifyUrl());
        PushNotificationServer pushNotificationServer = new PushNotificationServer(
                InetAddress.getByName(uri.getHost()),
                uri.getPort(),
				notificationConsumer
        );
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(pushNotificationServer::strat);
        return pushNotificationServer;
    }


}
