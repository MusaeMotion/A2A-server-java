package com.a2a.demo.agent.client.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author labidc@hotmail.com
 * @date 2025/7/1  16:22
 * @description
 */
@Entity
@Table(name = "remote_agent_classification")
@Data
@NoArgsConstructor
public class RemoteAgentClassificationEntity {


	/**
	 * 主键id
	 */
	@Id
	private String id;

	/**
	 * 分类名称
	 */
	private String classificationName;

	/**
	 * 构建对象
	 * @param id
	 */
	public static RemoteAgentClassificationEntity newEntity(String id){
		RemoteAgentClassificationEntity remoteAgentClassification = new RemoteAgentClassificationEntity();
		remoteAgentClassification.setId(id);
		return remoteAgentClassification;
	}
}
