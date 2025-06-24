package com.musaemotion.a2a.agent.host.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musaemotion.a2a.agent.host.constant.AppEventType;
import com.musaemotion.a2a.agent.host.event.AgentAppEvent;
import com.musaemotion.a2a.agent.host.model.AgentRunningStreamModel;
import com.musaemotion.a2a.common.constant.MetaDataKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/20  16:28
 * @description 默认远程运行流监听
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DefaultRemoteAgentRunningStreamPublisher implements RemoteAgentRunningStreamPublisher {

	/**
	 * spring ApplicationEventPublisher
	 */
	private final ApplicationEventPublisher publisher;

	/**
	 * json工具
	 */
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void publisher(String text, String taskId, Map<String, Object> taskSendMetadata)  {
		try {
			AgentRunningStreamModel runningStreamModel = AgentRunningStreamModel.builder().text(text).taskId(taskId).metadata(taskSendMetadata).build();
			AgentAppEvent agentAppEvent = new AgentAppEvent(this, this.objectMapper.writeValueAsString(runningStreamModel), AppEventType.RUNNING, taskSendMetadata.get(MetaDataKey.CUR_AGENT_NAME).toString());
			this.publisher.publishEvent(agentAppEvent);
		}catch (JsonProcessingException e) {
			log.error("JsonProcessingException:{}", e.getMessage());
		}
	}
}
