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

package com.a2a.demo.agent.client.entity;

import com.musaemotion.a2a.agent.host.model.service.AgentCardVo;
import com.vladmihalcea.hibernate.type.json.JsonType;
import com.musaemotion.a2a.common.AgentCard;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.beans.BeanUtils;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.entity
 * @project：A2A
 * @date：2025/4/29 15:59
 * @description：请完善描述
 */
@Entity
@Table(name = "remote_agent")
@Data
@NoArgsConstructor
public class RemoteAgentEntity {

    /**
     * 主键id
     */
    @Id
    private String id;

    /**
     * 智能体名称
     */
    private String name;

    /**
     * 智能体显示名称
     */
    private String displayName;

    /**
     * 描述
     */
    private String description;

    /**
     * 智能体地址
     */
    private String address;

    /**
     * 智能体 card 信息
     */
    @Type(JsonType.class)
    @Column(length = 1000, columnDefinition = "json")
    private AgentCard agentCard;

    /**
     * 构建对象
     * @param id
     */
    public static RemoteAgentEntity newRemoteAgent(String id, AgentCard agentCard){
        RemoteAgentEntity remoteAgent = new RemoteAgentEntity();
        remoteAgent.setId(id);
        remoteAgent.setAgentCard(agentCard);
        remoteAgent.setAddress(agentCard.getUrl());
        remoteAgent.setDescription(agentCard.getDescription());
        remoteAgent.setName(agentCard.getName());
        remoteAgent.setDisplayName(agentCard.getName());
        return remoteAgent;
    }

    /**
     * 构建Dto对象
     * @return
     */
    public AgentCardVo toAgentCard(){
        AgentCardVo agentCardVo = new AgentCardVo();
        BeanUtils.copyProperties(this.getAgentCard(), agentCardVo);
        agentCardVo.setId(this.getId());
        return agentCardVo;
    }
}
