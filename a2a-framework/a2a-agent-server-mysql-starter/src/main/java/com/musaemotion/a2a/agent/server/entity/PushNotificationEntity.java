package com.musaemotion.a2a.agent.server.entity;

import com.musaemotion.a2a.common.base.Common;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/13  16:08
 * @description 通知实体
 */
@Entity
@Table(name = "push_notification")
@Data
@NoArgsConstructor
public class PushNotificationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "url", length = 400)
	private String url;

	@Column(name = "token", length = 1000)
	private String token;

	@Column(name = "task_id", length = 36)
	private String taskId;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private Common.AuthenticationInfo authentication;


	public Common.PushNotificationConfig to(){
		Common.PushNotificationConfig  pushNotificationConfig = new Common.PushNotificationConfig();
		pushNotificationConfig.setUrl(this.url);
		pushNotificationConfig.setToken(this.token);
		pushNotificationConfig.setAuthentication(this.authentication);
		return pushNotificationConfig;
	}
}
