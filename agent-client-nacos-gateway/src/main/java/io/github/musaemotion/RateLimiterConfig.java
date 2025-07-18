package io.github.musaemotion;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/17  15:08
 * @description 限流相关配置
 */
@Configuration
public class RateLimiterConfig {

	/**
	 * redis限流
	 * @return
	 */
	@Bean
	RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(100, 200);
	}

	/**
	 * 限流维度，简单设置,ip
	 * @return
	 */
	@Bean
	KeyResolver remoteAddrKeyResolver() {
		return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
	}
}
