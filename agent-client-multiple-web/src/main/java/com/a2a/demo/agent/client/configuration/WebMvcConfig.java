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

package com.a2a.demo.agent.client.configuration;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.configuration
 * @project：a2a-github
 * @date：2025/5/22 11:44
 * @description：请完善描述
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	/**
	 * 集成前端
	 * 匹配所有的静态访问都跳转到 资源文件夹下面static目录
	 * @param registry
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/static/");
	}


	/**
	 * 集成前端
	 * 404页面跳转 解决vue前端集成打包时使用
	 * @return
	 */
	@Bean
	public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer(){
		return factory -> {
			ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
			factory.addErrorPages(error404Page);
		};
	}
}
