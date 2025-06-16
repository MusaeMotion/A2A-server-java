package com.musaemotion.a2a.agent.server.repository;

import com.musaemotion.a2a.agent.server.entity.PushNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/13  16:09
 * @description 推送通知数据库操作
 */
@Repository
public interface PushNotificationRepository extends JpaRepository<PushNotificationEntity, Integer> {

	Optional<PushNotificationEntity> findByTaskId(String taskId);
}
