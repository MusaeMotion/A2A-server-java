package io.github.musaemotion.entity;

import com.musaemotion.a2a.agent.server.mcp.model.McpConfig;
import com.musaemotion.a2a.common.utils.GuidUtils;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/22  11:53
 * @description mcp 服务配置
 */
@Entity
@Table(name = "mcp_server")
@Data
@NoArgsConstructor
public class McpServerEntity {

	/**
	 * 主键id
	 */
	@Id
	private String id;

	/**
	 * mcp名称
	 */
	private String displayName;

	/**
	 * mcp名称
	 */
	private String name;

	/**
	 * 说明
	 */
	private String description;

	/**
	 * mcp配置
	 */
	@Type(JsonType.class)
	@Column(length = 1000, columnDefinition = "json")
	private McpConfig<?> mcpConfig;
	/**
	 * 是否启用
	 */
	private Boolean enable;

	/**
	 * 构建对象
	 * @param displayName
	 * @param mcpConfig
	 * @return
	 */
	public static McpServerEntity create(String displayName, McpConfig<?> mcpConfig) {
		McpServerEntity  mcpServerEntity = new McpServerEntity();
		mcpServerEntity.setId(GuidUtils.createGuid());
		mcpServerEntity.setName(mcpConfig.getName());
		mcpServerEntity.setDescription(mcpConfig.getDescription());
		mcpServerEntity.setDisplayName(displayName);
		mcpServerEntity.setMcpConfig(mcpConfig);
		mcpServerEntity.setEnable(true);
		return mcpServerEntity;
	}
}
