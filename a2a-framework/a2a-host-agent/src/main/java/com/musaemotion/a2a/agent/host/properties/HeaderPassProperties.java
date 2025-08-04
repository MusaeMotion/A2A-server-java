package com.musaemotion.a2a.agent.host.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/8/4  10:44
 * @description 透传头配置
 */
@Component
@ConfigurationProperties(prefix = "header.pass")
public class HeaderPassProperties {

	/**
	 * 是否开启透传，默认 false
	 */
	private boolean enabled = false;

	/**
	 * 需要透传的请求头名称列表，默认空列表
	 */
	private List<String> headers = Collections.emptyList();

	/* ---------- getter / setter ---------- */

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<String> getHeaders() {
		return headers == null ? Collections.emptyList() : headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
}
