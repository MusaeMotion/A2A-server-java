package io.github.musaemotion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/16  11:38
 * @description 网关
 */
@SpringBootApplication
@Slf4j
public class AgentClientGateWayApplication {

	/**
	 * 启动项
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(AgentClientGateWayApplication.class, args);
	}


}
