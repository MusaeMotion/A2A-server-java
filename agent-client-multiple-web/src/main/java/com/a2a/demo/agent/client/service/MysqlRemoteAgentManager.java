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

import com.a2a.demo.agent.client.entity.RemoteAgentEntity;
import com.a2a.demo.agent.client.repository.RemoteAgentRepository;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.common.AgentCard;
import com.musaemotion.a2a.agent.host.model.service.SearchRemoteAgentDto;
import com.musaemotion.a2a.agent.host.manager.AbstractRemoteAgentManager;
import com.musaemotion.a2a.common.utils.GuidUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
public class MysqlRemoteAgentManager extends AbstractRemoteAgentManager {

    private RemoteAgentRepository repository;

    @Autowired
    public MysqlRemoteAgentManager(RemoteAgentRepository repository){
        this.repository = repository;
    }

    /**
     *
     * @param input
     */
    @Transactional(rollbackFor = Exception.class)
    protected void save(AgentCard input) {
        var op = this.repository.findByName(input.getName());
        RemoteAgentEntity remoteAgent = null;
        if (op.isPresent()) {
          log.warn("该智能体已经存在, 更新操作");
          remoteAgent = (RemoteAgentEntity) op.get();
          BeanUtils.copyProperties(input, remoteAgent);
        }else{
          remoteAgent = RemoteAgentEntity.newRemoteAgent(GuidUtils.createGuid(), input);
        }

        this.repository.save(remoteAgent);
    }

    /**
     *
     * @param agentName
     * @return
     */
    public Optional<AgentCard> get(String agentName) {
        var op = this.repository.findByName(agentName);
        if (op.isEmpty()) {
           return Optional.empty();
        }
        return Optional.of(
                op.get().getAgentCard()
        );
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        ids.forEach(id -> {
            this.delete(id);
        });
    }

    /**
     *
     * @return
     */
    public List<AgentCard> listAll() {
        return this.repository.findAll().stream().map(item->item.toAgentCard()).collect(Collectors.toList());
    }

    /**
     *
     * @param searchInput
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<AgentCard> pageList(SearchRemoteAgentDto searchInput, int pageNum, int pageSize) {

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
