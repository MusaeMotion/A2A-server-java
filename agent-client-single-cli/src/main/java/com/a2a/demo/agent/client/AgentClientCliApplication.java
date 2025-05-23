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

package com.a2a.demo.agent.client;

import com.musaemotion.a2a.common.AgentCard;
import com.musaemotion.a2a.common.utils.GuidUtils;
import com.musaemotion.a2a.agent.client.A2ACardResolver;
import com.musaemotion.a2a.agent.client.A2aClient;
import com.musaemotion.a2a.agent.client.server.PushNotificationServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.URI;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client
 * @project：A2A
 * @date：2025/4/25 10:17
 * @description：请完善描述
 */
@SpringBootApplication
@Slf4j
public class AgentClientCliApplication implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(AgentClientCliApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 构建参数
        ArgsModel argsModel = Utils.buildArgs(args);
        try {
            // 获取AgentCard
            A2ACardResolver cardResolver = new A2ACardResolver(argsModel.getAgentUrl());
            AgentCard card = cardResolver.getAgentCard();
            log.info(card.toJson());

            // 本地通知的地址
            URI uri = new URI(argsModel.getNotificationEndpoint());

            // 通知Server
            PushNotificationServer pushNotificationServer = null;
            // 如果启动通知，构建通知Server
            if (argsModel.getEnableNotification()) {
                // 启动通知的http服务
                pushNotificationServer = new PushNotificationServer(
                        InetAddress.getByName(uri.getHost()),
                        uri.getPort()
                );
                // 注册到通知里面
                pushNotificationServer.registerAgent(card.getName(), argsModel.getAgentUrl());
            }

            // A2a客户端
            A2aClient a2aClient = new A2aClient(card);

            // 当前请求会话id
            String sessionId = argsModel.getSession() == null || "0".equals(argsModel.getSession()) ? GuidUtils.createShortRandomGuid() : argsModel.getSession().toString();

            // 启动控制台命令
            ConsoleClientCommand command = new ConsoleClientCommand(
                    pushNotificationServer,
                    a2aClient,
                    sessionId,
                    card.getCapabilities().streaming(),
                    argsModel.getHistory()
            );
            command.run();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
