package io.github.musaemotion;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static junit.framework.Assert.assertNotNull;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/22  14:52
 * @description
 */
@AutoConfigureMockMvc
@SpringBootTest(classes = AgentServerMcpApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Slf4j
public class ServiceTest {


	@Autowired
	protected Environment environment;


	protected ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 测试前
	 */
	@BeforeEach
	public void contextLoads() {
		// 验证上下文是否正确加载
		assertNotNull(environment);
		// 当前上下文信息
		log.warn("Active profiles: {} ", String.join(", ", environment.getActiveProfiles()));
	}

}
