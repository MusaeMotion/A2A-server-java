package io.github.musaemotion;

import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/18  10:09
 * @description 启动完成之后，通过nacos 里面注册的服务，自动注册到远程智能体
 */
@Component
@RequiredArgsConstructor
public class StartupRunnerRegA2A {

	/**
	 * Nacos 服务
	 */
	private final NacosService nacosService;

	/**
	 *
	 */
    private final RestTemplate restTemplate;

	/**
	 *
	 */
	private final static String CARD_URL_TPL = "musaemotion.a2a.server.url: '%s'";
	/**
	 * 获取统一网关的URL路径
	 */
	@Value("${musaemotion.a2a.gateway}")
	private String gateWayUrl;


	@EventListener(ApplicationReadyEvent.class)
	public void callAfterRoutesReady() {
		// 公共请求头
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		this.nacosService.getA2aPaths().forEach(a2aPath -> {
			String a2aRegUrl = this.gateWayUrl + a2aPath.path();
			RegisterAgentDto dto = new RegisterAgentDto(a2aRegUrl);
			try {

				// 修改配置中心设置
				this.nacosService.updateConfig(a2aPath.serviceName(), String.format(CARD_URL_TPL, a2aRegUrl));

				// 序列化
				String json = new ObjectMapper().writeValueAsString(dto);
				HttpEntity<String> entity = new HttpEntity<>(json, headers);
				// 发送 POST
				this.restTemplate.postForObject(
						"lb://" + this.nacosService.getHostAgent() + "/api/remote-agent",
						entity,
						String.class);


			} catch (JsonProcessingException e) {
				throw new RuntimeException("序列化 RegisterAgentDto 失败", e);
			}catch (NacosException e){
				throw new RuntimeException("Nacos 无法访问", e);
			}
		});
	}

	/**
	 * 注册智能体
	 * @param url
	 */
	public record RegisterAgentDto(
			String url
	) {}

}
