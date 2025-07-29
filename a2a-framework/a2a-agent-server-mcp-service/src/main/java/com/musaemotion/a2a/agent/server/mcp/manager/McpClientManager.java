package com.musaemotion.a2a.agent.server.mcp.manager;

import com.musaemotion.a2a.agent.server.mcp.constant.McpConnType;
import com.musaemotion.a2a.agent.server.mcp.constant.McpSseClientType;
import com.musaemotion.a2a.agent.server.mcp.model.ConfigMarker;
import com.musaemotion.a2a.agent.server.mcp.model.McpConfig;
import com.musaemotion.a2a.agent.server.mcp.model.SseConfig;
import com.musaemotion.a2a.agent.server.mcp.model.StdioConfig;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/25  13:10
 * @description 动态工具抽象类，继承该类的 bean 都会被扫描
 */
@Service
@Slf4j
public class McpClientManager {

	/* ---------- 两仓 ---------- */
	private final Map<String, McpSyncClient>  syncCache  = new ConcurrentHashMap<>();
	private final Map<String, McpAsyncClient> asyncCache = new ConcurrentHashMap<>();

	/* ---------- 统一入口 ---------- */
	public void refresh(List<McpConfig<?>> configs) {
		if (CollectionUtils.isEmpty(configs)) return;

		configs.forEach(cfg -> {
			McpConnType type = cfg.getConnType();
			switch (type) {
				case SSE -> handle(cfg, (SseConfig) cfg.getConfig());
				case STDIO -> handle(cfg, (StdioConfig) cfg.getConfig());
			}
		});


		var keepNames = configs.stream()
				.map(McpConfig::getName)
				.collect(Collectors.toSet());

		// 同步缓存：删除多余
		syncCache.keySet().removeIf(name -> {
			if (!keepNames.contains(name)) {
				shutdownGracefully(name, syncCache.remove(name));
				return true;
			}
			return false;
		});

		// 异步缓存：删除多余
		asyncCache.keySet().removeIf(name -> {
			if (!keepNames.contains(name)) {
				shutdownGracefully(name, asyncCache.remove(name));
				return true;
			}
			return false;
		});
	}

	/* ---------- 获取Client ---------- */
	public Optional<McpSyncClient>  getSync(String name)  { return Optional.ofNullable(syncCache.get(name)); }
	public Optional<McpAsyncClient> getAsync(String name) { return Optional.ofNullable(asyncCache.get(name)); }

	/**
	 * 获取所有 Sync Client
	 * @return
	 */
	public List<McpSyncClient> getSync(){
		return syncCache.values().stream().collect(Collectors.toList());
	}

	/**
	 * 获取所有 Async Client
	 * @return
	 */
	public List<McpAsyncClient> getAsync(){
		return asyncCache.values().stream().collect(Collectors.toList());
	}

	/* ---------- 删除 Client ---------- */
	public void removeSync(String name)  { shutdownGracefully(name, syncCache.remove(name)); }
	public void removeAsync(String name) { shutdownGracefully(name, asyncCache.remove(name)); }

	/**
	 * 测试是否通过
	 * @param mcpConfig
	 * @return
	 */
	public String test(McpConfig mcpConfig){
		McpConnType type = mcpConfig.getConnType();
		switch (type) {
			case SSE -> {
				McpSyncClient client = buildSync(mcpConfig.getConfig());
				try {
					client.initialize();
					return "";
				}catch(Exception e) {
					return e.getMessage();
				}
			}
			case STDIO -> {
				McpAsyncClient client = buildAsync(mcpConfig.getConfig());
				try {
					client.initialize();
					return "";
				}catch(Exception e) {
					return e.getMessage();
				}
			}
			default -> {
				return "配置文件错误";
			}
		}
	}

	/**
	 * 内部分发：按 clientType 决定同步还是异步
	 * @param cfg
	 * @param innerCfg
	 */
	private void handle(McpConfig<?> cfg, ConfigMarker innerCfg) {
		McpSseClientType clientType = innerCfg.getClientType();
		String name = cfg.getName();

		switch (clientType) {
			case SYNC  -> handleSync(name, innerCfg);
			case ASYNC -> handleAsync(name, innerCfg);
			default    -> log.warn("Config [{}] missing clientType", name);
		}
	}

	/**
	 * 同步处理
	 * @param name
	 * @param cfg
	 */
	private void handleSync(String name, ConfigMarker cfg) {
		McpSyncClient old = syncCache.get(name);
		if (old != null && !isAlive(old)) {
			shutdownGracefully(name, old);
			old = null;
		}
		if (old == null) {
			McpSyncClient client = buildSync(cfg);
			try {
				client.initialize();
				syncCache.put(name, client);
				log.info("Sync client [{}] created & cached", name);
			}catch(Exception e) {
				log.info("Sync client [{}] conn error", name);
			}
		}
	}

	/**
	 * 异步处理
	 * @param name
	 * @param cfg
	 */
	private void handleAsync(String name, ConfigMarker cfg) {
		McpAsyncClient old = asyncCache.get(name);
		if (old != null && !isAlive(old)) {
			shutdownGracefully(name, old);
			old = null;
		}
		if (old == null) {
			McpAsyncClient client = buildAsync(cfg);
			client.initialize();
			asyncCache.put(name, client);
			log.info("Async client [{}] created & cached", name);
		}
	}
	/**
	 * 构造异步Client
	 * @param cfg
	 * @return
	 */
	private McpSyncClient buildSync(ConfigMarker cfg) {
		if (cfg instanceof SseConfig c) {
			return buildSseSync(c);
		} else if (cfg instanceof StdioConfig c) {
			return buildStdioSync(c);
		} else {
			throw new IllegalArgumentException("Unknown config type");
		}
	}

	/**
	 * 构造异步 Client
	 * @param cfg
	 * @return
	 */
	private McpAsyncClient buildAsync(ConfigMarker cfg) {
		if (cfg instanceof SseConfig c) {
			return buildSseAsync(c);
		} else if (cfg instanceof StdioConfig c) {
			return buildStdioAsync(c);
		} else {
			throw new IllegalArgumentException("Unknown config type");
		}

	}

	/**
	 * 构造sse同步Client
	 * @param cfg
	 * @return
	 */
	private McpSyncClient buildSseSync(SseConfig cfg) {
		String url = buildSseUrl(cfg);
		return McpClient.sync(HttpClientSseClientTransport.builder(url).build()).build();
	}

	/**
	 * 构造sse异步Client
	 * @param cfg
	 * @return
	 */
	private McpAsyncClient buildSseAsync(SseConfig cfg) {
		String url = buildSseUrl(cfg);
		return McpClient.async(HttpClientSseClientTransport.builder(url).build()).build();
	}

	/**
	 * 构造标准同步Client
	 * @param cfg
	 * @return
	 */
	private McpSyncClient buildStdioSync(StdioConfig cfg) {
		ServerParameters params = ServerParameters.builder(cfg.getCommand())
				.args(cfg.getArgs())
				.env(cfg.getEnv())
				.build();
		return McpClient.sync(new StdioClientTransport(params)).requestTimeout(Duration.ofSeconds(10)).build();
	}

	/**
	 * 构造标准异步Client
	 * @param cfg
	 * @return
	 */
	private McpAsyncClient buildStdioAsync(StdioConfig cfg) {
		ServerParameters params = ServerParameters.builder(cfg.getCommand())
				.args(cfg.getArgs())
				.env(cfg.getEnv())
				.build();
		return McpClient.async(new StdioClientTransport(params)).build();
	}

	/**
	 * 构造sseUrl
	 * @param cfg
	 * @return
	 */
	private String buildSseUrl(SseConfig cfg) {
		String base = cfg.getUrl().replaceAll("/+$", "");
		String ep = cfg.getSseEndpoint().replaceAll("^/+", "");
		return base + "/" + ep;
	}

	/**
	 * 判断链接是否存在
	 * @param client
	 * @return
	 */
	private boolean isAlive(Object client) {
		try {
			if (client instanceof McpAsyncClient ac) {
				// ac.listTools().block(Duration.ofSeconds(3));
				var ret = ac.ping();
				log.warn("Health check Async ping [{}]: {}", client, ret.block());
			} else if (client instanceof McpSyncClient sc) {
				var ret = sc.ping();
				log.warn("Health check Sync ping [{}]: {}", client, ret);
			}
			return true;
		} catch (Exception e) {
			log.warn("Health check failed for [{}]: {}", client, e.getMessage());
			return false;
		}
	}

	/**
	 * 安全关闭
	 * @param name
	 * @param client
	 */
	private void shutdownGracefully(String name, Object client) {
		try {
			if (client instanceof McpAsyncClient c) c.closeGracefully();
			if (client instanceof McpSyncClient c) c.closeGracefully();
			log.info("Client [{}] closed", name);
		} catch (Exception e) {
			log.error("Error closing client [{}]", name, e);
		}
	}

}
