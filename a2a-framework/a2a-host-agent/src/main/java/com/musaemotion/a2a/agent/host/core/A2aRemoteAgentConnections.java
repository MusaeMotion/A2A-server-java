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

package com.musaemotion.a2a.agent.host.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.musaemotion.a2a.common.AgentCard;
import com.musaemotion.a2a.common.IMetadata;
import com.musaemotion.a2a.common.event.TaskArtifactUpdateEvent;
import com.musaemotion.a2a.common.event.TaskEvent;
import com.musaemotion.a2a.common.event.TaskStatusUpdateEvent;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.common.request.SendTaskRequest;
import com.musaemotion.a2a.common.request.SendTaskStreamingRequest;
import com.musaemotion.a2a.common.request.params.TaskSendParams;
import com.musaemotion.a2a.common.response.SendTaskResponse;
import com.musaemotion.a2a.common.response.SendTaskStreamingResponse;
import com.musaemotion.a2a.common.constant.TaskState;
import com.musaemotion.a2a.common.utils.GuidUtils;
import com.musaemotion.a2a.agent.client.A2aClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.ConnectableFlux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.musaemotion.a2a.common.constant.MetaDataKey.LAST_MESSAGE_ID;
import static com.musaemotion.a2a.common.constant.MetaDataKey.MESSAGE_ID;


/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.client
 * @project：A2A
 * @date：2025/4/27 22:37
 * @description：A2a远程Agent连接
 */
@Slf4j
public class A2aRemoteAgentConnections {

	/**
	 * a2a client
	 */
	private A2aClient a2aClient;

	/**
	 * agent Card
	 */
	@Getter
	private AgentCard agentCard;

	/**
	 * agentCard,
	 * @param agentCard
	 */
	public A2aRemoteAgentConnections(AgentCard agentCard){
		this.a2aClient = new A2aClient(agentCard);
		this.agentCard = agentCard;
	}


	/**
	 * agent 支持 call
	 * @param taskSendParams
	 * @param callback
	 * @return
	 */
	private Task callAgent(TaskSendParams taskSendParams, ISendTaskCallback callback){
		SendTaskRequest sendTaskRequest = SendTaskRequest.newInstance(taskSendParams);
		SendTaskResponse sendTaskResponse = this.a2aClient.sendTask(sendTaskRequest);
		if(sendTaskResponse.getResult() == null && sendTaskResponse.getError() != null) {
			throw new RuntimeException(sendTaskResponse.getError().getMessage());
		}
		// 合并任务相关的元数据
		this.mergeMetadata(sendTaskResponse.getResult(), taskSendParams);

		// 合并消息相关的元数据
		if (sendTaskResponse.getResult() != null && sendTaskResponse.getResult().getStatus() != null && sendTaskResponse.getResult().getStatus().getMessage() != null) {
			// 合并到消息对象上
			this.mergeMetadata(sendTaskResponse.getResult().getStatus().getMessage(), taskSendParams.getMessage());
			var message = sendTaskResponse.getResult().getStatus().getMessage();
			if (isNullOrEmpty(message.getMetadata())) {
				message.setMetadata(Maps.newConcurrentMap());
			}
			//
			if (message.getMetadata().containsKey(MESSAGE_ID)) {
				message.getMetadata().put(LAST_MESSAGE_ID, message.getMetadata().get(MESSAGE_ID));
			}
			message.getMetadata().put(MESSAGE_ID, GuidUtils.createShortRandomGuid());
		}
		callback.sendTaskCallback(sendTaskResponse.getResult());
		return sendTaskResponse.getResult();
	}


	/**
	 * agent 支持 stream 请求
	 * @param taskSendParams
	 * @param callback
	 * @return
	 */
	private Task streamAgent(TaskSendParams taskSendParams,  ISendTaskCallback callback){
		AtomicReference<Task> taskModel = new AtomicReference<>();
		// 流请求
		ConnectableFlux<SendTaskStreamingResponse> responseConnectableFlux = this.a2aClient.sendTaskStreaming(SendTaskStreamingRequest.newInstance(taskSendParams));
		responseConnectableFlux.subscribe(sendTaskStreamingResponse -> {
			if (sendTaskStreamingResponse.getError() != null) {
				log.error("stream error => {}", sendTaskStreamingResponse.getError().getMessage());
				return;
			}
			if (sendTaskStreamingResponse.getResult() instanceof TaskEvent taskEvent) {
				this.mergeMetadata(taskEvent, taskSendParams);
			}
			if (sendTaskStreamingResponse.getResult() instanceof TaskStatusUpdateEvent taskStatusUpdateEvent) {
				if (taskStatusUpdateEvent.getStatus().getMessage() == null) {
					taskStatusUpdateEvent.getStatus().setMessage(new Common.Message());
				}
				//  合并状态里面的消息元数据，到状态消息里面的消息
				this.mergeMetadata(taskStatusUpdateEvent.getStatus().getMessage(), taskSendParams.getMessage());
				var message = taskStatusUpdateEvent.getStatus().getMessage();

				if (isNullOrEmpty(message.getMetadata())) {
					message.setMetadata(Maps.newConcurrentMap());
				}
				if (message.getMetadata().containsKey(MESSAGE_ID)) {
					message.getMetadata().put(LAST_MESSAGE_ID, message.getMetadata().get(MESSAGE_ID));
				}
				// message.getMetadata().put(MESSAGE_ID, GuidUtils.createShortRandomGuid());
				callback.sendTaskCallback(taskStatusUpdateEvent);
				taskModel.set(Task.from(taskStatusUpdateEvent));
				if (taskStatusUpdateEvent.getDone()) {
					log.error("任务状态更新完成");
				}
			}
			/**
			 * 工件更新
			 */
			if (sendTaskStreamingResponse.getResult() instanceof TaskArtifactUpdateEvent taskArtifactUpdateEvent) {
				callback.sendTaskCallback(taskArtifactUpdateEvent);
				taskModel.set(Task.from(taskArtifactUpdateEvent));
			}
		});
		responseConnectableFlux.connect();
		responseConnectableFlux.blockLast();
		return taskModel.get();
	}

	/**
	 * 发送任务, 会判断是流请求，还是 同步请求
	 * @param taskSendParams
	 * @param callback
	 */
	public Task sendTask(TaskSendParams taskSendParams, ISendTaskCallback callback) {
		// 任务提交中
		callback.sendTaskCallback(Task.from(taskSendParams));

		if (this.getAgentCard().getCapabilities().streaming()) {
			return this.streamAgent(taskSendParams, callback);
		}

		return this.callAgent(taskSendParams, callback);
	}

	/**
	 * 合并metaData
	 * @param target
	 * @param source
	 */
	private void mergeMetadata(IMetadata target, IMetadata source) {
		if (isNullOrEmpty(target.getMetadata()) || isNullOrEmpty(source.getMetadata())) {
			return;
		}
		if (isNullOrEmpty(target.getMetadata())) {
			target.setMetadata(new HashMap<>(source.getMetadata()));
		} else {
			// 否则，将 sourceMetadata 的内容合并到 targetMetadata 中
			target.getMetadata().putAll(source.getMetadata());
		}
	}


	/**
	 * 判断map 为空
	 * @param map
	 * @return
	 */
	public static boolean isNullOrEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}
}
