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

package com.musaemotion.a2a.common.request.params;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.musaemotion.a2a.common.IMetadata;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.constant.MediaType;
import com.musaemotion.a2a.common.constant.MessageRole;
import com.musaemotion.a2a.common.notification.PushNotificationAuth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSendParams implements Serializable, IMetadata {

	/**
	 * 任务taskId
	 */
	private String id;

	/**
	 * 1. sessionId, 也就是交谈Id, 同一个 session 会产生多个消息（inputMessage），每个消息 都有自己独立 messageId (对于task来说，他是inputMessageId),
	 * 2. inputMessageId 消息可能会触发 产生多个task 任务,每个 task 都有自己而独立的 taskId 和 messageId(这里和前面 inputMessageId不同),
	 * 3. sessionId 1 对多 inputMessage, inputMessage 1 对多 task
	 */
	private String sessionId;

	/**
	 * 任务传递的消息
	 */
	private Common.Message message;

	/**
	 * host智能体支持的输出方式
	 */
	private List<MediaType> acceptedOutputModes;

	/**
	 * 消息配置
	 */
	private Common.PushNotificationConfig pushNotification;

	/**
	 * 获取远程智能体历史消息记录的长度
	 */
	private Integer historyLength;

	/**
	 * 其他扩展字段
	 */
	private Map<String, Object> metadata;


	/**
	 * 构建任务文本消息
	 * @param taskId
	 * @param sessionId
	 * @param text
	 * @return
	 */
	public static TaskSendParams newUserTextInstance(String taskId, String sessionId, String text) {
		return newUserTextInstance(taskId, sessionId, text, null);
	}
	/**
	 * 构建任务文本消息
	 * @param taskId
	 * @param sessionId
	 * @param text
	 * @return
	 */
	public static TaskSendParams newUserTextInstance(String taskId, String sessionId, String text, String pushNotificationUrl) {

		return newUserTextInstance(
				taskId,
				sessionId,
				Common.Message.newMessage(
						MessageRole.USER,
						Lists.newArrayList(new Common.TextPart(text)),
						Maps.newConcurrentMap()
				),
				Lists.newArrayList(MediaType.TEXT),
				Maps.newConcurrentMap(),
				pushNotificationUrl
		);
	}


	/**
	 *
	 * @param taskId
	 * @param sessionId
	 * @param message
	 * @param acceptedOutputModes
	 * @param sendMetadata
	 * @param pushNotificationUrl
	 * @return
	 */
	public static TaskSendParams newUserTextInstance(String taskId, String sessionId, Common.Message message, List<MediaType> acceptedOutputModes, Map<String, Object> sendMetadata, String pushNotificationUrl) {
		var taskSendParamsBuilder = TaskSendParams.builder()
				.id(taskId)
				.sessionId(sessionId)
				.message(message)
				.acceptedOutputModes(acceptedOutputModes);

		if (!StringUtils.isEmpty(pushNotificationUrl)) {
			// 访问时的验证方式
			var authenticationInfo = new Common.AuthenticationInfo();
			authenticationInfo.setSchemes(Lists.newArrayList(PushNotificationAuth.AUTH_SCHEMES));
			// 访问时的密钥
			authenticationInfo.setCredentials("test-token123");
			taskSendParamsBuilder = taskSendParamsBuilder.pushNotification(
					Common.PushNotificationConfig.builder()
							.url(pushNotificationUrl)
							.token("push-test-token123")
							.authentication(authenticationInfo)
							.build());
		}
		taskSendParamsBuilder = taskSendParamsBuilder.metadata(sendMetadata);
		return taskSendParamsBuilder.build();
	}
}
