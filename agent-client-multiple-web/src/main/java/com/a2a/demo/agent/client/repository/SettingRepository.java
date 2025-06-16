package com.a2a.demo.agent.client.repository;

import com.a2a.demo.agent.client.entity.SettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/13  18:00
 * @description 系统提示词
 */
@Repository
public interface SettingRepository extends JpaRepository<SettingEntity, String>, JpaSpecificationExecutor<SettingEntity> {

	/**
	 *
	 * @param id
	 * @return
	 */
	Optional<SettingEntity> findById(String id);

}
