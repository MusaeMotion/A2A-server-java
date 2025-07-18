package io.github.musaemotion;

import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.client.RestTemplate;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/17  17:16
 * @description gateway路由构建
 */

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AgentGateWayConfig {

	/**
	 * 限流对象
	 */
	private final RedisRateLimiter redisRateLimiter;

	/**
	 * Nacos 服务
	 */
	private final NacosService nacosService;



	/**
	 * 路由规则
	 * @param builder
	 * @return
	 */
	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) throws NacosException {
		// 构建host agent 路由
		var routeBuildable = this.nacosService.buildHostAgentRoutes(builder.routes());

		// 构建其他远程智能体路由
		routeBuildable = this.nacosService.buildAgentRoutes(routeBuildable, this.redisRateLimiter);
		// 需要授权相关api
		routeBuildable = routeBuildable.route("path-auth-api", r -> r.path("/auth-api/**")
				.filters(f -> f.requestRateLimiter(c -> c.setRateLimiter(this.redisRateLimiter)))
				.uri("http://127.0.0.1:10001"));
		RouteLocator routeLocator = routeBuildable.build();
		log.info("customRouteLocator: {}",routeLocator.getRoutes().count());
		return routeLocator;
	}


	/**
	 * Security Filter Chain
	 * @param http
	 * @return
	 */
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		return http
				.csrf(csrf -> csrf.disable())
				.authorizeExchange(ex -> ex
						.pathMatchers("/auth-api/**").authenticated()
						.anyExchange().permitAll()
				)
				.httpBasic(Customizer.withDefaults())
				.build();
	}
	/**
	 * 登录授权
	 * @return
	 */
	@Bean
	public MapReactiveUserDetailsService reactiveUserDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
		return new MapReactiveUserDetailsService(user);
	}

	/**
	 *
	 * @return
	 */
	@Bean
	@LoadBalanced   // 开启负载均衡
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
