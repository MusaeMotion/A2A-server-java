package io.github.musaemotion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.agent.server.agent.AgentRequest;
import com.musaemotion.a2a.agent.server.agent.AgentService;
import com.musaemotion.a2a.agent.server.mcp.manager.ToolManager;
import com.musaemotion.a2a.agent.server.mcp.model.McpConfig;
import com.musaemotion.a2a.agent.server.mcp.model.SearchMcpConfig;
import com.musaemotion.a2a.agent.server.mcp.model.SseConfig;
import com.musaemotion.a2a.agent.server.mcp.model.StdioConfig;
import io.github.musaemotion.entity.McpServerEntity;
import io.github.musaemotion.service.MysqlMcpServerManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.musaemotion.a2a.common.constant.MetaDataKey.CONVERSATION_ID;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/22  14:57
 * @description mcp智能体
 */
@Slf4j
public class McpAgentServiceTest extends ServiceTest {

	@Autowired
	private MysqlMcpServerManager mysqlMcpServerManager;

	@Autowired
	private ToolManager toolManager;

	@Autowired
	private AgentService agentService;

	/**
	 * 创建mcp server
	 */
	@Test
	void testAddMcpServer() {
		var mcpConfig = McpConfig.<SseConfig>builder()
				.name("my-weather-server")
				.description("获取天气相关的信息")
				.config(SseConfig.builder().url("http://127.0.0.1:7777").build())
				.build();
		McpServerEntity mcpServerEntity = McpServerEntity.create("天气服务", mcpConfig);
		this.mysqlMcpServerManager.save(mcpServerEntity);
	}
	/**
	 * 创建mcp server
	 */
	@Test
	void testAddMcpFileServer() {
		var mcpConfig = McpConfig.<StdioConfig>builder()
				.name("filesystem")
				.description("获取文件列表和文件内容服务")
				.config(
						StdioConfig.builder()
								.args(List.of("-y", "@modelcontextprotocol/server-filesystem", getDbPath()))
								.command("npx.cmd")
								.build()
				)
				.build();
		McpServerEntity mcpServerEntity = McpServerEntity.create("文件系统管理", mcpConfig);
		this.mysqlMcpServerManager.save(mcpServerEntity);
	}

	/**
	 * 该方法返回的是
	 * @return
	 */
	private static String getDbPath() {
		return Paths.get(System.getProperty("user.dir"), "target").toString();
	}

	/**
	 * 删除 mcp server
	 */
	@Test
	void testDeleteMcpServer() {
		this.mysqlMcpServerManager.delete("98312017-0198-31201d82-40288a76-0000");
	}

	/**
	 * 批量获取配置列表
	 */
	@Test
	void testSearchMcpConfigs() {
		var list = this.mysqlMcpServerManager.searchMcpConfigs(new SearchMcpConfig());
		log.info("testSearchMcpConfigs: {}",list.toString());
	}

	/**
	 * 获取控件列表
	 */
	@Test
	void testGetTools() throws JsonProcessingException {
		var tools = this.toolManager.getTools();
		ObjectMapper mapper = new ObjectMapper();
		log.info("testGetTools: {}",
		mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
			  tools.stream().map(item->item.getToolDefinition().name()).collect(Collectors.toUnmodifiableList())
			)
		);
	}
	/**
	 * 获取控件列表
	 */
	@Test
	void testToolBasics() throws JsonProcessingException {
		var toolBasics = this.toolManager.getMcpBasics();
		ObjectMapper mapper = new ObjectMapper();
		log.info("testToolBasics: {}",
			mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
					toolBasics
			)
		);
	}

	/**
	 * 调用Call
	 * @throws JsonProcessingException
	 */
	@Test
	void testCallSse() throws JsonProcessingException {
		String id = UUID.randomUUID().toString();
		AgentRequest agentRequest = AgentRequest.builder()
				.text("请给告诉这个坐标的天气情况 latitude：47.6062, longitude：-122.3321")
				.parts(Lists.newArrayList())
				.metadata(Map.of(CONVERSATION_ID, id))
				.sessionId(id)
				.build();
		var response = this.agentService.call(agentRequest);
		ObjectMapper mapper = new ObjectMapper();
		log.info("testCall: {}",
				mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
						response
				)
		);
	}

	/**
	 * 调用Call
	 * @throws JsonProcessingException
	 */
	@Test
	void testCallStdio() throws JsonProcessingException {
		String id = UUID.randomUUID().toString();
		AgentRequest agentRequest = AgentRequest.builder()
				.text("请给我列出 "+getDbPath()+" 文件夹下有哪些文件")
				.parts(Lists.newArrayList())
				.metadata(Map.of(CONVERSATION_ID, id))
				.sessionId(id)
				.build();
		var response = this.agentService.call(agentRequest);
		ObjectMapper mapper = new ObjectMapper();
		log.info("testCall2: {}",
				mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
						response
				)
		);
	}
}
