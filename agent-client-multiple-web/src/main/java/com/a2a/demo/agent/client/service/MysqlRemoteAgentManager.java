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

package com.a2a.demo.agent.client.service;

import com.a2a.demo.agent.client.dto.AgentCardExtend;
import com.a2a.demo.agent.client.dto.SearchRemoteAgent;
import com.a2a.demo.agent.client.entity.RemoteAgentEntity;
import com.a2a.demo.agent.client.repository.RemoteAgentRepository;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.agent.host.listener.RemoteAgentRunningStreamPublisher;
import com.musaemotion.a2a.agent.host.manager.AbstractRemoteAgentManager;
import com.musaemotion.a2a.common.AgentCard;
import com.musaemotion.a2a.common.utils.GuidUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.service
 * @project：A2A
 * @date：2025/4/29 15:55
 * @description：远程代码注册管理实现
 */
@Service
@Slf4j
public class MysqlRemoteAgentManager extends AbstractRemoteAgentManager<AgentCardExtend> {

    private RemoteAgentRepository repository;

	/**
	 *
	 * @param repository
	 * @param runningStreamListeners
	 */
    @Autowired
    public MysqlRemoteAgentManager(RemoteAgentRepository repository, List<RemoteAgentRunningStreamPublisher> runningStreamListeners){
		super(runningStreamListeners);
        this.repository = repository;
    }

	/**
	 * 保存落库
	 * @param input
	 */
	@Transactional(rollbackFor = Exception.class)
    protected void save(AgentCard input) {
        var op = this.repository.findByName(input.getName());
        RemoteAgentEntity remoteAgent = null;
        if (op.isPresent()) {
          remoteAgent = op.get().from(input);
        }else{
          remoteAgent = RemoteAgentEntity.newRemoteAgent(GuidUtils.createGuid(), input);
        }
        this.repository.save(remoteAgent);
    }

	/**
	 * 获取当前AgentCard
	 * @param id
	 * @return
	 */
	public Optional<AgentCardExtend> getById(String id) {
        var op = this.repository.findById(id);
        if (op.isEmpty()) {
           return Optional.empty();
        }
        return Optional.of(
				op.get().toAgentCard()
        );
    }
	/**
	 * 获取当前AgentCard
	 * @param Id
	 * @return
	 */
	public void changeAgentEnable(String Id) {
		var op = this.repository.findById(Id);
		if (op.isEmpty()) {
			return;
		}
		RemoteAgentEntity remoteAgent = op.get();
		remoteAgent.setEnable(!remoteAgent.getEnable());
		this.repository.save(remoteAgent);
	}

	/**
	 * 修改AgentCard
	 * @param Id
	 * @param input
	 */
	public void updateAgentCard(String Id, AgentCard input) {
		var op = this.repository.findById(Id);
		if (op.isEmpty()) {
			return;
		}
		RemoteAgentEntity remoteAgent = op.get();
		remoteAgent.setAgentCard(input);
		remoteAgent.setDescription(input.getDescription());
		remoteAgent.setAddress(input.getUrl());
		this.repository.save(remoteAgent);
	}

    /**
     *
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        this.repository.deleteById(id);
    }

    /**
     * 批量删除
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        ids.forEach(id -> {
            this.delete(id);
        });
    }

    /**
     * 获取所有AgentCard
     * @return
     */
    public List<AgentCardExtend> listAll() {
        return this.repository.findAll().stream().filter(item->item.getEnable()).map(item->item.toAgentCard()).collect(Collectors.toList());
    }

    /**
     * 远程智能体分页
     * @param searchInput
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<AgentCardExtend> pageList(SearchRemoteAgent searchInput, int pageNum, int pageSize) {

        Specification specification = new Specification<RemoteAgentEntity>() {
            @Override
            public Predicate toPredicate(Root<RemoteAgentEntity> entityRoot, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = Lists.newArrayList();
                if (!StringUtils.isEmpty(searchInput.getName())) {
                    predicates.add(criteriaBuilder.like(entityRoot.get("name"),"%" +  searchInput.getName().trim()+ "%"));
                }
                if (!StringUtils.isEmpty(searchInput.getDescription())) {
                    predicates.add(criteriaBuilder.like(entityRoot.get("description"), "%" + searchInput.getDescription().trim() + "%"));
                }

                return criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]))
                ).getRestriction();
            }
        };
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.Direction.DESC, "id");
        Page<RemoteAgentEntity> page = this.repository.findAll(specification, pageable);
        return page.map(remoteAgent -> remoteAgent.toAgentCard());
    }
}
