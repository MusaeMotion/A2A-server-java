package com.musaemotion.a2a.agent.server.mcp.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.agent.server.mcp.filter.DefaultIToolFilter;
import com.musaemotion.a2a.agent.server.mcp.filter.IToolFilter;
import com.musaemotion.a2a.agent.server.mcp.model.McpBasics;
import com.musaemotion.a2a.agent.server.mcp.model.McpConfig;
import com.musaemotion.a2a.agent.server.mcp.model.SearchMcpConfig;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/25  15:09
 * @description 动态工具服务
 */
/**
 * MCP 动态发现中心。
 * 负责根据配置实时刷新并聚合所有 MCP Server 的工具、资源、提示词、能力等信息，
 * 对外提供统一的只读视图。
 */

@Service
public class ToolManager {

	private final McpClientManager mcpClientManager;

	private final IMcpServerManager<SearchMcpConfig> mcpServerManager;

	// mcp 自带工具
	private final List<ToolCallbackProvider> toolProviders = new ArrayList<>();

	private final List<McpBasics> mcpBasicsList = Lists.newArrayList();

	private IToolFilter toolFilter;


	@Autowired
	public ToolManager(McpClientManager mcpClientManager,
					   IMcpServerManager<SearchMcpConfig> mcpServerManager,
					   @Autowired(required = false) IToolFilter toolFilter) {
		this.mcpClientManager = mcpClientManager;
		this.mcpServerManager = mcpServerManager;
		this.toolFilter = toolFilter;
		if (toolFilter == null) {
			this.toolFilter = new DefaultIToolFilter();
		}
	}

	/**
	 *
	 */
	@PostConstruct
	public void init() {
		// 同样会在依赖注入完成后被调用
		this.reloadAll();
	}

	/**
	 * 全量重新加载所有 MCP 元数据。
	 * 线程安全：调用期间会短暂阻塞并发读，保证聚合操作原子性。
	 */
	public synchronized void reloadAll() {
	    SearchMcpConfig searchMcpConfig = new SearchMcpConfig();
		searchMcpConfig.setEnable(true);
		List<McpConfig<?>> configs = this.mcpServerManager.searchMcpConfigs(searchMcpConfig);
		this.refreshClients(configs);
		this.reloadTools();
		this.reloadCapabilities(configs);
	}

	/** 1. 根据最新配置重建所有 MCP Client（同步+异步） */
	private void refreshClients(List<McpConfig<?>> configs) {
		this.mcpClientManager.refresh(configs);
	}

	/**
	 * 构建能力对象
	 * @param mcpBasics
	 * @param serverCapabilities
	 */
	private void loadCapabilities(McpBasics mcpBasics, McpSchema.ServerCapabilities serverCapabilities, Object mcpClient) {
		ToolCapabilityLoader loader;
		if (mcpClient instanceof McpAsyncClient mcpAsyncClient) {
			loader = new ToolAsyncCapabilityLoader(mcpBasics, serverCapabilities, mcpAsyncClient);
		} else if (mcpClient instanceof McpSyncClient mcpSyncClient) {
			loader = new ToolSyncCapabilityLoader(mcpBasics, serverCapabilities, mcpSyncClient);
		} else {
			throw new IllegalArgumentException("Unsupported client type: " + mcpClient.getClass().getName());
		}
		// 调用加载方法
		loader.loadTools();
		loader.loadResources();
		loader.loadPrompts();

	}

	/**
	 * 重新加载工具，初始化链接，构建 ToolCallbackProvider 提供者
	 */
	private void reloadTools() {
		this.toolProviders.clear();
		if (!mcpClientManager.getSync().isEmpty()) {
			this.toolProviders.add(new SyncMcpToolCallbackProvider(this.toolFilter.mcpSyncClientToolFilter(), this.mcpClientManager.getSync()));
		}
		if (!mcpClientManager.getAsync().isEmpty()) {
			this.toolProviders.add(new AsyncMcpToolCallbackProvider(this.toolFilter.mcpAsyncClientToolFilter(), this.mcpClientManager.getAsync()));
		}
	}



	/**
	 * 聚合重新加载工具所有
	 * @param configs
	 */
	private void reloadCapabilities(List<McpConfig<?>> configs) {
		this.mcpBasicsList.clear();
		configs.forEach(config -> {
			var mcpBasics = McpBasics.builder()
					.mcpName(config.getName())
					.mcpDescription(config.getDescription())
					.build();

		    var optionalAsyncClient = this.mcpClientManager.getAsync(config.getName());
			if(optionalAsyncClient.isPresent()) {
				McpAsyncClient mcpAsyncClient = optionalAsyncClient.get();
				this.loadCapabilities(mcpBasics, mcpAsyncClient.getServerCapabilities(), mcpAsyncClient);
			}
			var optionalSyncClient = this.mcpClientManager.getSync(config.getName());
			if(optionalSyncClient.isPresent()) {
				McpSyncClient mcpSyncClient = optionalSyncClient.get();
				this.loadCapabilities(mcpBasics, mcpSyncClient.getServerCapabilities(), mcpSyncClient);
			}
			if(optionalAsyncClient.isEmpty() && optionalSyncClient.isEmpty()) {
				return;
			}
			this.mcpBasicsList.add(mcpBasics);
		});
	}

	/** 获取工具基础描述列表 */
	public List<McpBasics> getMcpBasics() {
		return this.mcpBasicsList;
	}

	/**
	 * 获取所有tools
	 * @return
	 */
	public List<ToolCallback> getTools() {
		List<ToolCallback> mcpTools = this.toolProviders.stream()
				.flatMap(p -> Arrays.stream(p.getToolCallbacks()))
				.collect(Collectors.toUnmodifiableList());

		return mcpTools;
	}

	// mcp前缀
	private String prefix = "JavaSDKMCPClient_";

	/** 根据名称精确查找工具 */
	public Optional<ToolCallback> findToolByName(String toolName) {
		return this.getTools().stream()
				.filter(t ->{
					if(t.getToolDefinition().name().equals(toolName))
						return true;
					if (t.getToolDefinition().name().startsWith(prefix) && t.getToolDefinition().name().replace(prefix,"").equals(toolName))
						return true;
					return false;
				})
				.findFirst();
	}

	/** 将工具基础描述序列化为 JSON 字符串 */
	public String getMcpBasicsJson() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		PropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept("resources", "prompts");
		SimpleFilterProvider filterProvider = new SimpleFilterProvider();
		filterProvider.addFilter("toolFilter", filter);
		objectMapper.setFilterProvider(filterProvider);
		return objectMapper.writeValueAsString(getMcpBasics());
	}

}
