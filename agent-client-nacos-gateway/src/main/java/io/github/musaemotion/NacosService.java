package io.github.musaemotion;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.client.config.NacosConfigService;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static io.github.musaemotion.CircuitBreakerService.CIRCUIT_ID;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/17  14:30
 * @description nacos 服务
 */
@Service
@RequiredArgsConstructor
public class NacosService {

	/**
	 * 配置项
	 */
	private final NacosDiscoveryProperties nacosDiscoveryProperties;

	/**
	 * 配置项
	 */
	private final NacosConfigProperties nacosConfigProperties;

	/**
	 * 配置中心文件后缀
	 */
	private final static String DATA_ID_FILE_EXT ="yaml";


	/**
	 *
	 * @param path
	 * @param serviceName
	 */
	public record AgentInfo (
			String path,
			String serviceName
	) {}

	/**
	 * a2a 相对路径
	 */
	@Getter
	private List<AgentInfo> a2aPaths = Lists.newArrayList();

	/**
	 * 构建登录使用的配置项
	 * @return
	 */
	private Properties buildDiscoveryAuthProperties() {
		// 添加认证参数
		Properties properties = new Properties();
		properties.put("serverAddr", this.nacosDiscoveryProperties.getServerAddr());
		properties.put("username", this.nacosDiscoveryProperties.getUsername());
		properties.put("password", this.nacosDiscoveryProperties.getPassword());
		properties.put("namespace", this.nacosDiscoveryProperties.getNamespace());
		return properties;
	}

	/**
	 * 构建登录使用的配置项
	 * @return
	 */
	private Properties buildConfigAuthProperties() {
		// 添加认证参数
		Properties properties = new Properties();
		properties.put("serverAddr", this.nacosConfigProperties.getServerAddr());
		properties.put("username", this.nacosConfigProperties.getUsername());
		properties.put("password", this.nacosConfigProperties.getPassword());
		properties.put("namespace", this.nacosConfigProperties.getNamespace());
		return properties;
	}

	/**
	 * 获取 agent Service 列表
	 * @return
	 */
	public List<String> listAgentServices() throws NacosException {
		NamingService namingService = NacosFactory.createNamingService(this.buildDiscoveryAuthProperties());
		var list = namingService.getServicesOfServer(1, Integer.MAX_VALUE, nacosDiscoveryProperties.getGroup());
		var agentList = list.getData().stream().filter(item -> item.startsWith("agent-")).collect(Collectors.toUnmodifiableList());
		return agentList;
	}

	/**
	 * host agent 服务名称
	 * @return
	 * @throws NacosException
	 */
	public String getHostAgent() throws NacosException {
		NamingService namingService = NacosFactory.createNamingService(this.buildDiscoveryAuthProperties());
		var list = namingService.getServicesOfServer(1, Integer.MAX_VALUE, nacosDiscoveryProperties.getGroup());
		if (list.getCount() == 0) {
			return "";
		}
		var hostList = list.getData().stream().filter(item -> item.startsWith("host-")).collect(Collectors.toUnmodifiableList());
		return hostList.get(0);
	}

	/**
	 * 构建 buildAgentRoutes
	 * @param builder
	 * @param redisRateLimiter
	 * @return
	 * @throws NacosException
	 */
	public RouteLocatorBuilder.Builder buildAgentRoutes(RouteLocatorBuilder.Builder builder, RedisRateLimiter redisRateLimiter) throws NacosException {
		var agentServices = this.listAgentServices();
		this.a2aPaths = Lists.newArrayList();
		for (String serviceName : agentServices) {
			// a2a 访问该智能体路径
			String a2aPath = "/agent/" + serviceName;
			this.a2aPaths.add(new AgentInfo(a2aPath, serviceName));
			builder = builder.route(serviceName, r -> r.path(a2aPath + "/**")
					.filters(f -> {
						GatewayFilterSpec gatewayFilterSpec = f.stripPrefix(2);
						// 限制流配置
						gatewayFilterSpec = gatewayFilterSpec.requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter));
						// 熔断-降级响应
						gatewayFilterSpec.circuitBreaker(c -> c.setName(CIRCUIT_ID).setFallbackUri("forward:/circuit-fallback"));
						return f;
					})
					.uri("lb://" + serviceName));

		}
		return builder;
	}

	/**
	 * 主机智能体路由
	 * @param builder
	 * @return
	 * @throws NacosException
	 */
	public RouteLocatorBuilder.Builder buildHostAgentRoutes(RouteLocatorBuilder.Builder builder) throws NacosException {
		String hostAgent = this.getHostAgent();
		// host-agent 路由
		builder = builder.route(hostAgent , r -> r.path("/api/**")
				.uri("lb://" + hostAgent));
		return builder;
	}

	/**
	 *
	 * @throws NacosException
	 */
	public void updateConfig(String serviceName, String content) throws NacosException {
		String dataId = serviceName + "." + DATA_ID_FILE_EXT;
		ConfigService cfg = NacosFactory.createConfigService(this.buildConfigAuthProperties());
		// 写配置（会覆盖旧值）
		cfg.publishConfig(dataId, this.nacosConfigProperties.getGroup(), content, ConfigType.YAML.getType());
	}
}
