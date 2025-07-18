package io.github.musaemotion;

import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import com.musaemotion.a2a.agent.server.service.PromptProvider;
import com.musaemotion.a2a.common.constant.RemoteAgentConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author zhangmijia
 * @version 1.0.0
 * @date 2025/6/16  15:02
 * @description 默认Redis服务实现，依赖于Spring Data Redis (因为agent Card 实现了 配置了modify-prompt : true，表示可以通过host agent修改提示词，约定和host agent 用redis 传递信息，这里用redis实现)
 */
public class RedisPromptProvider implements PromptProvider {



    private static final Logger logger = LoggerFactory.getLogger(RedisPromptProvider.class);

    private StringRedisTemplate redisTemplate;

	private A2aServerProperties serverProperties;


    public RedisPromptProvider(StringRedisTemplate redisTemplate, A2aServerProperties serverProperties) {
        this.redisTemplate = redisTemplate;
		this.serverProperties = serverProperties;
		logger.info("Redis连接已配置，使用 RedisPromptProvider");
    }
    

	@Override
	public String getPrompt() {
		String key = String.format(RemoteAgentConstants.REDIS_PROMPT_PREFIX_TPL, this.serverProperties.getName());
		String value = redisTemplate.opsForValue().get(key);
		if (value == null) {
			return "";
		}
		logger.info("getPrompt: {} ", value);
		return value;
	}

	@Override
	public Boolean savePrompt(String prompt) {
		String key = String.format(RemoteAgentConstants.REDIS_PROMPT_PREFIX_TPL, this.serverProperties.getName());
		try {
			return redisTemplate.hasKey(key);
		} catch (Exception e) {
			logger.error("检查key是否存在时发生错误", e);
			return false;
		}
	}
}
