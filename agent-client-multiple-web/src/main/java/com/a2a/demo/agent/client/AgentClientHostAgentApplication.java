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

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client
 * @project：A2A
 * @date：2025/4/28 11:07
 * @description：启动类
 */
@ComponentScan("com.a2a.demo")
@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@RequiredArgsConstructor
public class AgentClientHostAgentApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AgentClientHostAgentApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {}

}
