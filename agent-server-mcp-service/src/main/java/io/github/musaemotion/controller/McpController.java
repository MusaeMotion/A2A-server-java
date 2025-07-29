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

package io.github.musaemotion.controller;


import com.musaemotion.a2a.agent.server.mcp.manager.McpClientManager;
import com.musaemotion.a2a.agent.server.mcp.manager.ToolManager;
import com.musaemotion.a2a.agent.server.mcp.model.McpConfig;
import com.musaemotion.a2a.agent.server.mcp.model.SearchMcpConfig;
import com.musaemotion.a2a.common.utils.GuidUtils;
import com.musaemotion.a2a.common.utils.PageUtils;
import com.musaemotion.a2a.common.web.PageInfo;
import com.musaemotion.a2a.common.web.Result;
import io.github.musaemotion.constant.Constant;
import io.github.musaemotion.entity.McpServerEntity;
import io.github.musaemotion.service.MysqlMcpServerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.controller
 * @project：A2A
 * @date：2025/4/28 11:55
 * @description：chat 控制器
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class McpController {

	private final MysqlMcpServerManager mysqlMcpServerManager;

	private final ToolManager toolManager;

	private final McpClientManager mcpClientManager;

	/**
	 * 分页
	 * @param searchInput
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@PostMapping(value = Constant.MCP_SERVER + "/{pageNum}/{pageSize}")
	public ResponseEntity pageList(@RequestBody SearchMcpConfig searchInput, @PathVariable("pageNum") Integer pageNum, @PathVariable("pageSize") Integer pageSize) {
		pageNum = pageNum - 1;
		var page = this.mysqlMcpServerManager.pageList(searchInput, pageNum, pageSize);
		PageInfo<McpServerEntity> pageInfo = PageUtils.springPageToMyPage(page);
		return ResponseEntity.ok(Result.buildSuccess(
				pageInfo
		));
	}
	/**
	 * 保存成功
	 * @param input
	 * @return
	 */
	@PostMapping(value = Constant.MCP_SERVER)
	public ResponseEntity create(@RequestBody McpServerEntity input) {
		input.setId(GuidUtils.createGuid());
		input.getMcpConfig().setName(input.getName());
		input.getMcpConfig().setDescription(input.getDescription());
		input.setEnable(Boolean.TRUE);
		this.mysqlMcpServerManager.save(input);
		this.toolManager.reloadAll();
		return ResponseEntity.ok(Result.buildSuccess(
				"保存成功"
		));
	}
	/**
	 * 保存成功
	 * @param input
	 * @return
	 */
	@PutMapping(value = Constant.MCP_SERVER)
	public ResponseEntity update(@RequestBody McpServerEntity input) {
		this.mysqlMcpServerManager.save(input);
		this.toolManager.reloadAll();
		return ResponseEntity.ok(Result.buildSuccess(
				"保存成功"
		));
	}

	/**
	 * 删除
	 * @param ids
	 * @return
	 */
	@DeleteMapping(value = Constant.MCP_SERVER)
	public ResponseEntity delete(@RequestParam("id") String ids) {
		if (StringUtils.hasText(ids)) {
			List<String> id = Arrays.stream(ids.split(",")).collect(Collectors.toList());
			this.mysqlMcpServerManager.delete(id);
			this.toolManager.reloadAll();
		}
		return ResponseEntity.ok(Result.buildSuccess());
	}
	/**
	 * 切换状态
	 * @param id
	 * @return
	 */
	@PutMapping(Constant.MCP_SERVER+"/enable/{id}")
	public ResponseEntity changeEnable(@PathVariable String id) {
		this.mysqlMcpServerManager.changeEnable(id);
		return ResponseEntity.ok(Result.buildSuccess());
	}
	/**
	 * 测试通过是否通过
	 * @param mcpConfig
	 * @return
	 */
	@PostMapping(value = Constant.MCP_SERVER+"/test")
	public ResponseEntity test(@RequestBody McpConfig<?> mcpConfig) {
		String error = this.mcpClientManager.test(mcpConfig);
		if (StringUtils.hasText(error)) {
			return ResponseEntity.ok(Result.buildError(9999, error));
		}
		return ResponseEntity.ok(Result.buildSuccess());
	}

}
