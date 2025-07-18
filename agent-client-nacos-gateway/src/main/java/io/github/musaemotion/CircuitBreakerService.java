package io.github.musaemotion;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/17  15:02
 * @description 熔断相关配置
 */
@RestController
public class CircuitBreakerService {

	/**
	 * 熔断降级操作
	 * @return
	 */
	@RequestMapping("/circuit-fallback")
	public String circuitFallback() {
		return "暂时无法响应你的请求";
	}

	/**
	 * 熔断器ID
	 */
	public static String CIRCUIT_ID ="slowRequest";


	/**
	 * 全局熔断设置，不适合大模型，因为请求时间都很长
	 * @return
	 */
	@Bean
	public CircuitBreakerRegistry circuitBreakerRegistry() {
		CircuitBreakerConfig config = CircuitBreakerConfig.custom()
				.slidingWindowSize(10)
				.minimumNumberOfCalls(5)
				.failureRateThreshold(50.0f)
				.slowCallDurationThreshold(Duration.ofSeconds(2))
				.waitDurationInOpenState(Duration.ofSeconds(10))
				.build();

		return CircuitBreakerRegistry.of(config);
	}

	/**
	 * 自定义名称的熔断配置，不适合大模型，因为请求时间都很长
	 * @return
	 */
	@Bean
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> slowRequestCustomizer() {
		// 1. 熔断规则
		CircuitBreakerConfig cbCfg = CircuitBreakerConfig.custom()
				.slidingWindowSize(10)                    // 最近 10 次调用
				.minimumNumberOfCalls(5)                  // 至少 5 次才统计
				.failureRateThreshold(50.0f)              // 失败率 ≥ 50% 触发 OPEN
				.slowCallDurationThreshold(Duration.ofSeconds(2)) // 响应>2s 算慢调用
				.slowCallRateThreshold(100.0f)            // 100% 慢调用触发 OPEN
				.waitDurationInOpenState(Duration.ofSeconds(10))  // OPEN 持续 10s
				.permittedNumberOfCallsInHalfOpenState(3) // HALF_OPEN 允许 3 次探测
				.build();

		// 2. 超时时间（可选）
		TimeLimiterConfig tlCfg = TimeLimiterConfig.custom()
				.timeoutDuration(Duration.ofSeconds(3))
				.build();

		// 3. 绑定到 Gateway 的熔断器工厂
		return factory -> factory.configure(
				builder -> builder
						.circuitBreakerConfig(cbCfg)
						.timeLimiterConfig(tlCfg),
				CIRCUIT_ID);
	}
}
