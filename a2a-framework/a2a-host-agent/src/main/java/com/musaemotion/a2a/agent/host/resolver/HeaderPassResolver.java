package com.musaemotion.a2a.agent.host.resolver;

import com.musaemotion.a2a.agent.host.properties.HeaderPassProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/8/4  10:46
 * @description 透传头解析
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HeaderPassResolver {

	private final HeaderPassProperties properties;

	/**
	 * 根据配置提取需要透传的请求头：
	 *   1. 未开启时直接返回空 Map
	 *   2. 忽略大小写匹配
	 *   3. 过滤掉 null 或 blank 值
	 */
	public Map<String, String> resolve(HttpServletRequest request) {
		if (!properties.isEnabled()) {
			return Collections.emptyMap();
		}

		Map<String, String> result = new HashMap<>();
		for (String cfgHeader : properties.getHeaders()) {
			if (!StringUtils.hasText(cfgHeader)) {
				continue;
			}
			// 忽略大小写查找（常见 HTTP 头大小写不敏感）
			String value = request.getHeader(cfgHeader);
			if (StringUtils.hasText(value)) {
				result.put(cfgHeader, value);
			}
		}
		log.debug("HeaderPassResolver Resolved Headers: {}", result);
		return Collections.unmodifiableMap(result);
	}
}
