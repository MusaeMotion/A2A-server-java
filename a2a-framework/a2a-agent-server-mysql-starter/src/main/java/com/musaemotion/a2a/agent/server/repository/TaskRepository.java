package com.musaemotion.a2a.agent.server.repository;

import com.musaemotion.a2a.agent.server.entity.AgentTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/13  16:10
 * @description 任务
 */
@Repository
public interface TaskRepository extends JpaRepository<AgentTaskEntity, String> {
}
