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

package com.a2a.demo.agent.client.repository;

import com.a2a.demo.agent.client.entity.RemoteAgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.repository
 * @project：A2A
 * @date：2025/4/29 15:58
 * @description：请完善描述
 */
@Repository
public interface RemoteAgentRepository extends JpaRepository<RemoteAgentEntity, String>, JpaSpecificationExecutor<RemoteAgentEntity> {

    /**
     * 根据智能体名检索
     * @param name
     * @return
     */
    Optional<RemoteAgentEntity> findByName(String name);



}
