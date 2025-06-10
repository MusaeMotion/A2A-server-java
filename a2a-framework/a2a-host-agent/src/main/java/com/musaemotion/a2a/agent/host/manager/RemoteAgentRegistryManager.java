package com.musaemotion.a2a.agent.host.manager;

import com.musaemotion.a2a.agent.client.server.PushNotificationServer;
import com.musaemotion.a2a.agent.host.model.service.RegisterAgentDto;
import com.musaemotion.a2a.common.AgentCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/10  11:37
 * @description 远程智能体注册中心
 */
@Service
public class RemoteAgentRegistryManager {


	/**
	 * 远程智能体管理器
	 */
	private AbstractRemoteAgentManager remoteAgentManager;


	/**
	 * 消息通知服务
	 */
	private PushNotificationServer pushNotificationServer;

	/**
	 *
	 * @param remoteAgentManager
	 * @param pushNotificationServer
	 */
	@Autowired
	public RemoteAgentRegistryManager(AbstractRemoteAgentManager remoteAgentManager, @Autowired(required = false)  PushNotificationServer pushNotificationServer){
       this.remoteAgentManager = remoteAgentManager;
	   this.pushNotificationServer = pushNotificationServer;
	}

	/**
	 * 注册中心注册智能体
	 * @param input
	 * @return
	 */
	public AgentCard registerAgent(RegisterAgentDto input) {
		AgentCard agentCard = this.remoteAgentManager.registerAgent("http://" + input.getUrl());
		if (this.pushNotificationServer != null) {
			this.pushNotificationServer.registerAgent(agentCard.getName(), agentCard.getUrl());
		}
		return agentCard;
	}

}
