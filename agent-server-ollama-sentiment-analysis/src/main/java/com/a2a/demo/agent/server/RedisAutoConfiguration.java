package com.a2a.demo.agent.server;

import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import com.musaemotion.a2a.agent.server.service.NoopPromptProvider;
import com.musaemotion.a2a.agent.server.service.PromptProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis自动配置类
 * 根据项目环境自动配置Redis服务实现
 */
@Configuration
@Order(-1)
@Slf4j
public class RedisAutoConfiguration {


	private A2aServerProperties serverProperties;

	@Autowired
	public RedisAutoConfiguration(A2aServerProperties serverProperties) {
		this.serverProperties = serverProperties;
	}

	// 当 RedisTemplate 存在且配置了 spring.data.redis.host 时创建真实实现
	@Bean("promptProvider")
	@Primary
	@ConditionalOnProperty(
			prefix = "spring.data.redis",
			name = "host",
			matchIfMissing = false // 必须存在该属性
	)
	public PromptProvider redisPromptProvider(StringRedisTemplate redisTemplate) {
		log.info("使用Redis提示词提供者");
		return new RedisPromptProvider(redisTemplate, this.serverProperties);
	}

	// 当 RedisTemplate 不存在 或 没有配置 spring.data.redis.host 时创建虚拟实现
	@Bean("promptProvider")
	@ConditionalOnProperty(
			prefix = "spring.data.redis",
			name = "host",
			matchIfMissing = true // 缺少该属性时匹配
	)
	@ConditionalOnMissingBean(name = "promptProvider") // 额外添加 @ConditionalOnMissingBean，确保只有在真实实现未创建时才创建虚拟实现
	public PromptProvider noopPromptProvider() {
		log.info("使用内存提示词提供者");
		return new NoopPromptProvider();
	}
}
