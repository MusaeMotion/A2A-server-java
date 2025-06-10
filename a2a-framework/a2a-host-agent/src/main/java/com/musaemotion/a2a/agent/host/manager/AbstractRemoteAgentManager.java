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

package com.musaemotion.a2a.agent.host.manager;

import com.musaemotion.a2a.agent.client.A2ACardResolver;
import com.musaemotion.a2a.agent.host.core.A2aRemoteAgentConnections;
import com.musaemotion.a2a.common.AgentCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.service
 * @project：A2A
 * @date：2025/4/29 15:26
 * @description：远程智能体
 */
public abstract class AbstractRemoteAgentManager <T extends AgentCard> {

    /**
     * 远程智能体连接
     */
    private Map<String, A2aRemoteAgentConnections> remoteAgentConnections = new HashMap<>();


    /**
     * 注册智能体落库
     * @param agentUrl
     * @return
     */
    public AgentCard registerAgent(String agentUrl) {
        // 获取A2A Card
        A2ACardResolver a2aCardResolver = new A2ACardResolver(agentUrl);
        // 获取 AgentCard
        AgentCard agentCard = a2aCardResolver.getAgentCard();
        // 落库保存
        this.save(agentCard);
        // 注册智能体远程连接
        this.registerRemoteAgentConnections(agentCard);
        // 返回A2A Card
        return agentCard;
    }

    /**
     * 注册到agent 连接，连接对象依赖了Agent Client, 发起远程调用
     * @param agentCard
     */
    private void registerRemoteAgentConnections(AgentCard agentCard) {
        var remoteConnection = new A2aRemoteAgentConnections(agentCard);
        this.remoteAgentConnections.put(agentCard.getName(), remoteConnection);
    }

    /**
     * 获取agent连接对象
     * @return
     */
    public Optional<A2aRemoteAgentConnections> getRemoteAgentConnections(String agentName){
        if(this.remoteAgentConnections.containsKey(agentName)){
            return Optional.of(this.remoteAgentConnections.get(agentName));
        }
        return Optional.empty();
    }

    /**
     * 刷新重新注册智能体，包括远程连接
     */
    public List<AgentCard> refreshRemoteAgentConnections() {
        // 清空已经有智能体连接
        this.remoteAgentConnections.clear();

        // 获取已经存在的智能体
        List<AgentCard> agentCards = (List<AgentCard>) this.listAll();

        // 重新注册所有的智能体连接
        agentCards.forEach(this::registerRemoteAgentConnections);

        return agentCards;
    }

    /**
     * 最好具有操作幂等性
     *
     * @param input
     * @return
     */
    protected abstract void save(AgentCard input);

    /**
     * 获取所有远程智能体
     * @return
     */
    public abstract List<T> listAll();

}
