package io.github.musaemotion.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	/**
	 * 开启跨域，主要是给host agent web管理后台配套前端使用
	 * @param registry
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") // 允许所有路径的跨域请求
				.allowedOrigins(
						"http://localhost:8000",
						"http://localhost:8001/",
						"http://localhost:5173",
						"http://localhost:8011",
						"http://192.168.10.247:8000",
						"http://192.168.10.247:8130",
						"http://localhost:10000"
				)// 允许的来源
				.allowedMethods("GET", "POST", "PUT", "DELETE") // 允许的 HTTP 方法
				.allowedHeaders("*") // 允许的请求头
				.allowCredentials(true) // 是否允许发送 Cookie
				.maxAge(3600); // 预检请求的缓存时间（秒）
	}
}
