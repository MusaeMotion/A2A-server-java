package io.github.musaemotion.repository;

import io.github.musaemotion.entity.McpServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/22  14:02
 * @description mcp server
 */
@Repository
public interface McpServerRepository extends JpaRepository<McpServerEntity, String>, JpaSpecificationExecutor<McpServerEntity> {

	Optional<McpServerEntity> findByName(String name);

}
