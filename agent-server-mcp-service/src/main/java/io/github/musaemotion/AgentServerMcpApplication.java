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

package io.github.musaemotion;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static io.github.musaemotion.constant.Constant.ENTITY_PAGE;
import static io.github.musaemotion.constant.Constant.REPOSITORY_PAGE;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client
 * @project：A2A
 * @date：2025/4/28 11:07
 * @description：启动类
 */
@SpringBootApplication
@EnableTransactionManagement
@RequiredArgsConstructor
@EntityScan(basePackages = ENTITY_PAGE)
@EnableJpaRepositories(REPOSITORY_PAGE)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class AgentServerMcpApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AgentServerMcpApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {}


}
