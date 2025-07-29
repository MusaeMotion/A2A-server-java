package io.github.musaemotion.service;

import com.musaemotion.a2a.agent.server.mcp.manager.IMcpServerManager;
import com.musaemotion.a2a.agent.server.mcp.model.McpConfig;
import com.musaemotion.a2a.agent.server.mcp.model.SearchMcpConfig;
import io.github.musaemotion.entity.McpServerEntity;
import io.github.musaemotion.repository.McpServerRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/22  11:43
 * @description mysql mcp 管理器
 */
@Service
@RequiredArgsConstructor
public class MysqlMcpServerManager implements IMcpServerManager<SearchMcpConfig> {

	private final McpServerRepository mcpServerRepository;

	/**
	 *
	 * @param id
	 */
	@Transactional(rollbackFor = Exception.class)
	public Optional<McpServerEntity> get(String id) {
		var optional = this.mcpServerRepository.findById(id);
		return optional;
	}

	/**
	 * 名称相同会覆盖的幂等性覆盖
	 * @param mcpServer
	 */
	@Transactional(rollbackFor = Exception.class)
	public void save(McpServerEntity mcpServer) {
		var optional = this.mcpServerRepository.findByName(mcpServer.getName());
		if (optional.isPresent()) {
			var oldMcpServer = optional.get();
		    String oldId = oldMcpServer.getId();
			BeanUtils.copyProperties(mcpServer, oldMcpServer);
			oldMcpServer.setId(oldId);
			this.mcpServerRepository.save(oldMcpServer);
			return;
		}
		this.mcpServerRepository.save(mcpServer);
	}

	/**
	 * 删除
	 * @param id
	 */
	@Transactional(rollbackFor = Exception.class)
	public void delete(String id) {
		this.mcpServerRepository.deleteById(id);
	}

	/**
	 * 批量删除
	 * @param ids
	 */
	@Transactional(rollbackFor = Exception.class)
	public void delete(List<String> ids) {
		this.mcpServerRepository.deleteAllById(ids);
	}

	/**
	 * 更新状态
	 * @param Id
	 */
	public void changeEnable(String Id) {
		var op = this.mcpServerRepository.findById(Id);
		if (op.isEmpty()) {
			return;
		}
		McpServerEntity mcpServer = op.get();
		mcpServer.setEnable(!mcpServer.getEnable());
		this.mcpServerRepository.save(mcpServer);
	}
	/**
	 *
	 * @param name
	 * @return
	 */
	@Override
	public Optional<McpConfig<?>> getMcpConfig(String name) {
		var optional = this.mcpServerRepository.findByName(name);
		if(optional.isPresent()) {
		     return Optional.of(
					 optional.get().getMcpConfig()
			 );
		}
		return Optional.empty();
	}

	/**
	 *
	 * @param searchInput
	 * @return
	 */
	@Override
	public List<McpConfig<?>> searchMcpConfigs(SearchMcpConfig searchInput) {
		var list = this.mcpServerRepository.findAll(this.buildSpecification(searchInput));
		return list.stream().map(it -> it.getMcpConfig()).collect(Collectors.toUnmodifiableList());
	}


	/**
	 *
	 * @param searchInput
	 * @return
	 */
	public static Specification<McpServerEntity> buildSpecification(SearchMcpConfig searchInput) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			// 搜索名称
			if (StringUtils.hasText(searchInput.getName())) {
				predicates.add(cb.equal(root.get("name"), searchInput.getName()));
			}

			if(searchInput.getEnable() != null) {
               predicates.add(cb.equal(root.get("enable"), searchInput.getEnable()));
			}
			// 搜索类型
			if(searchInput.getConnType()!=null) {
				Expression<String> jsonPath =
						cb.function("JSON_EXTRACT",
								String.class,
								root.get("mcpConfig"),
								cb.literal("$.connType"));

				// 注意：JSON_EXTRACT 返回带引号的字符串 "STDIO"
				return cb.equal(
						cb.function("JSON_UNQUOTE", String.class, jsonPath),
						searchInput.getConnType().name());
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}


	/**
	 * mcp 服务分类
	 * @param searchInput
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public Page<McpServerEntity> pageList(SearchMcpConfig searchInput, int pageNum, int pageSize) {
		Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.Direction.DESC, "id");
		return this.mcpServerRepository.findAll(this.buildSpecification(searchInput), pageable);
	}
}
