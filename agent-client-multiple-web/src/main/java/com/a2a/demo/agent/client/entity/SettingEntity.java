package com.a2a.demo.agent.client.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/13  17:51
 * @description 设置项表
 */
@Entity
@Table(name = "setting")
@Data
@NoArgsConstructor
public class SettingEntity {

	@Id
	private String id;

	/**
	 * 
	 */
    private String value;
}
