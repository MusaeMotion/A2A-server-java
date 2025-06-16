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

package com.musaemotion.a2a.agent.server.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.agent.server.agent.AgentGeneralResponse;
import com.musaemotion.a2a.agent.server.agent.AgentRequest;
import com.musaemotion.a2a.agent.server.agent.AgentResponseStatus;
import com.musaemotion.a2a.agent.server.agent.AgentService;
import com.musaemotion.a2a.agent.server.notification.PushNotificationSenderService;
import com.musaemotion.a2a.agent.server.properties.A2aServerProperties;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.common.base.base.JSONRPCMessage;
import com.musaemotion.a2a.common.base.base.JSONRPCResponse;
import com.musaemotion.a2a.common.base.error.ContentTypeNotSupportedError;
import com.musaemotion.a2a.common.base.error.InternalA2aError;
import com.musaemotion.a2a.common.base.error.InvalidParamsError;
import com.musaemotion.a2a.common.constant.MediaType;
import com.musaemotion.a2a.common.constant.MessageRole;
import com.musaemotion.a2a.common.constant.TaskState;
import com.musaemotion.a2a.common.event.TaskArtifactUpdateEvent;
import com.musaemotion.a2a.common.event.TaskStatusUpdateEvent;
import com.musaemotion.a2a.common.request.*;
import com.musaemotion.a2a.common.request.params.TaskIdParams;
import com.musaemotion.a2a.common.request.params.TaskQueryParams;
import com.musaemotion.a2a.common.request.params.TaskSendParams;
import com.musaemotion.a2a.common.response.*;
import com.musaemotion.a2a.common.response.result.TaskPushNotificationConfig;
import com.musaemotion.a2a.common.utils.PartUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.server.manager
 * @project：A2A
 * @date：2025/4/23 10:52
 * @description：请完善描述
 */
@Slf4j
public abstract class AbstractTaskManager implements ITaskManager, ITaskStore {

	/**
	 * 推送发送
	 */
	protected PushNotificationSenderService pushNotificationSenderService;

	/**
	 * TODO 互斥锁，原python 代码应该不是线程安全的，都加上lock排他锁, 所以这里暂照抄了，后面再来移除则移除
	 */
	protected Lock lock;

	/**
	 * 当下默认实现的内存任务管理器和Spring Agent 类型强依赖
	 */
	protected AgentService agentService;


	private A2aServerProperties a2aServerProperties;

	/**
	 * @param pushNotificationSenderService
	 */
	public AbstractTaskManager(PushNotificationSenderService pushNotificationSenderService, AgentService agentService, A2aServerProperties a2aServerProperties) {
		this.pushNotificationSenderService = pushNotificationSenderService;
		this.lock = new ReentrantLock();
		this.agentService = agentService;
		this.a2aServerProperties = a2aServerProperties;
	}


	/**
	 * 设置推送通知信息设置，验证推送地址正确定
	 *
	 * @param taskId
	 * @param notificationConfig
	 */
	protected Boolean setPushNotificationInfo(String taskId, Common.PushNotificationConfig notificationConfig) {
		try {
			// 验证通知 URL 的所有权
			boolean isVerified = this.pushNotificationSenderService.verifyPushNotificationUrl(notificationConfig.getUrl()).get();
			if (!isVerified) {
				return Boolean.FALSE;
			}
			// 调用父类的方法设置推送通知信息
			return this.setPushNotificationInfoToStore(taskId, notificationConfig);

		} catch (Exception e) {
			log.error("设置推送通知异常：{}", e.getMessage());
			return Boolean.FALSE;
		}
	}

	/**
	 * 推送消息到通知服务器
	 * @param task
	 * @return
	 */
	protected CompletableFuture<Void> sendTaskNotification(Task task) {

		Optional<Common.PushNotificationConfig> optional = this.getPushNotificationInfoForStore(task.getId());
		if (optional.isEmpty()) {
			return CompletableFuture.completedFuture(null);
		}
		Common.PushNotificationConfig pushNotificationConfig = optional.get();
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = objectMapper.convertValue(task, TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class));
		return CompletableFuture.runAsync((Runnable) () -> {
			this.pushNotificationSenderService.sendPushNotification(pushNotificationConfig.getUrl(), map, this.agentService.agentName());
		});

	}


	/**
	 * 验证请求，如果存在错误则返回
	 *
	 * @param taskSendParams
	 * @param requestId
	 * @return
	 */
	protected Optional<JSONRPCResponse> validateRequest(TaskSendParams taskSendParams, String requestId) {
		List<MediaType> agentSupportedContentTypes = this.agentService.supportedContentTypes();
		List<MediaType> intersection = taskSendParams.getAcceptedOutputModes().stream()
				.filter(agentSupportedContentTypes::contains)
				.collect(Collectors.toList());
		if (intersection.size() != agentSupportedContentTypes.size()) {
			return Optional.of(
					JSONRPCResponse.buildError(requestId, new ContentTypeNotSupportedError())
			);
		}
		return Optional.empty();
	}


	/**
	 * 创建一个task副本，并且指定聊天记录长度内容作为新task副本的历史记录
	 * @param task
	 * @param historyLength
	 * @return
	 */
	protected Task appendTaskHistory(Task task, Integer historyLength) {
		// 创建任务的副本， 深拷贝
		Task newTask = SerializationUtils.clone(task);
		// 根据 historyLength 截取历史记录
		if (historyLength != null && historyLength > -1) {
			// 截取最后 historyLength 条记录
			int start = Math.max(0, newTask.getHistory().size() - historyLength);
			newTask.setHistory(newTask.getHistory().subList(start, newTask.getHistory().size()));
		} else {
			// 如果 historyLength 为 null 或小于等于 0，清空历史记录
			newTask.setHistory(new ArrayList<>());
		}
		return newTask;
	}



	/**
	 * 更新任务的状态，并且更新工件信息
	 * @param taskId
	 * @param status
	 * @param artifacts
	 * @return
	 */
	protected Task updateTask(String taskId, Common.TaskStatus status, List<Common.Artifact> artifacts) {
		Optional<Task> optionalTask = this.getTaskForStore(taskId);
		if (optionalTask.isEmpty()) {
			throw new RuntimeException("Task does not exist");
		}
		Task task = optionalTask.get();
		task.setStatus(status);
		if (!CollectionUtils.isEmpty(artifacts)) {
			if (CollectionUtils.isEmpty(task.getArtifacts())) {
				task.setArtifacts(Lists.newArrayList());
			}
			task.getArtifacts().addAll(artifacts);
		}
		return task;
	}

	/**
	 * 创建和更新任务
	 * 如果对应的【任务】存在则在历史信息上追加当前请求消息
	 * 如果对应的【任务】不存在则创建一个【任务】
	 * @param taskSendParams
	 */
	protected void upsertTask(TaskSendParams taskSendParams) {

		Optional<Task> optionalTask = this.getTaskForStore(taskSendParams.getId());
		Task task = null;
		if (optionalTask.isEmpty()) {
			task = Task.builder()
					.id(taskSendParams.getId())
					.sessionId(taskSendParams.getSessionId())
					.status(new Common.TaskStatus(TaskState.SUBMITTED, taskSendParams.getMessage()))
					.history(Lists.newArrayList(taskSendParams.getMessage()))
					.metadata(taskSendParams.getMetadata())
					.build();
		} else {
			task = optionalTask.get();
			// 追加消息
			task.getHistory().add(taskSendParams.getMessage());
		}
		// 更新状态
		this.setTaskToStore(taskSendParams.getId(), task);
	}


	/**
	 * 处理 agent 响应
	 * @param sendTaskRequest
	 * @param agentGeneralResponse
	 * @return
	 */
	protected SendTaskResponse processAgentResponse(SendTaskRequest sendTaskRequest, AgentGeneralResponse agentGeneralResponse) {
		TaskSendParams taskSendParams = sendTaskRequest.getParams();
		String taskId = taskSendParams.getId();
		Integer historyLength = taskSendParams.getHistoryLength();
		Common.TaskStatus taskStatus = null;

		var parts = agentGeneralResponse.getParts();
		List<Common.Artifact> artifacts = new ArrayList<>();
		// 如果是输入状态
		if (agentGeneralResponse.getStatus().equals(AgentResponseStatus.INPUT_REQUIRED)) {
			taskStatus = Common.TaskStatus.builder()
					.state(TaskState.INPUT_REQUIRED)
					.message(Common.Message.builder().role(MessageRole.AGENT).parts(parts).build())
					.build();
		} else {
			// 如果是完成状态
			taskStatus = Common.TaskStatus.builder().state(TaskState.COMPLETED).build();
			artifacts.add(Common.Artifact.builder().parts(parts).build());
		}
		// 更新状态信息
		Task task = this.updateTask(
				taskId,
				taskStatus,
				artifacts.size() > 0 ? artifacts : null
		);
		// 追加历史信息
		Task taskResult = this.appendTaskHistory(task, historyLength);
		// 发送通知
		this.sendTaskNotification(task);
		// 返回追加后task
		return SendTaskResponse.buildResponse(sendTaskRequest.getId(), taskResult);
	}

	/**
	 * 构建任务状态
	 * @param taskId
	 * @param agentGeneralResponse
	 * @param artifacts
	 * @return
	 */
	private Common.TaskStatus buildTaskStatus(
			String taskId,
			AgentGeneralResponse agentGeneralResponse,
			List<Common.Artifact> artifacts) {

		Common.Message message = null;
		TaskState taskState = null;
		// 工作中，返回内容在message里面
		if(agentGeneralResponse.getStatus().equals(AgentResponseStatus.WORKING)) {
			taskState = TaskState.WORKING;
			message = Common.Message.builder().role(MessageRole.AGENT).parts(agentGeneralResponse.getParts()).build();
		}
		// 需要输入，返回内容在message里面
		if (agentGeneralResponse.getStatus().equals(AgentResponseStatus.INPUT_REQUIRED)) {
			taskState = TaskState.INPUT_REQUIRED;
			message = Common.Message.builder().role(MessageRole.AGENT).parts(agentGeneralResponse.getParts()).build();
		}

		if(agentGeneralResponse.getStatus().equals(AgentResponseStatus.COMPLETED)) {
			taskState = TaskState.COMPLETED;
			// 完成状态下添加工件
			artifacts.add(Common.Artifact.builder().parts(agentGeneralResponse.getParts()).build());
		}
		Common.TaskStatus taskStatus = Common.TaskStatus.builder().state(taskState).message(message).build();

		// 更新任务
		Task task = this.updateTask(taskId, taskStatus, artifacts.size() > 0 ? artifacts : null);

		// 发送通知消息
		this.sendTaskNotification(task);


		return taskStatus;
	}

	/**
	 * 获取任务响应
	 * @param request
	 * @return
	 */
	@Override
	public GetTaskResponse onGetTask(GetTaskRequest request) {
		TaskQueryParams taskQueryParams = request.getParams();
		lock.lock();
		try {
			Optional<Task> optionalTask = this.getTaskForStore(taskQueryParams.getId());
			if (optionalTask.isEmpty()) {
				return GetTaskResponse.buildTaskNotFoundError(request.getId());
			}
			var task = optionalTask.get();
			var taskResult = this.appendTaskHistory(
					task, taskQueryParams.getHistoryLength()
			);
			return GetTaskResponse.buildTask(request.getId(), taskResult);
		} finally {
			lock.unlock();  // 释放锁
		}
	}

	/**
	 * 取消任务
	 *
	 * @param request
	 * @return
	 */
	@Override
	public CancelTaskResponse onCancelTask(CancelTaskRequest request) {
		log.info("Cancelling task {}", request.getParams().getId());
		TaskIdParams taskIdParams = request.getParams();
		lock.lock();
		try {
			Optional<Task> optionalTask = this.getTaskForStore(taskIdParams.getId());
			if (optionalTask.isEmpty()) {
				return CancelTaskResponse.buildTaskNotFoundError(request.getId());
			}
			// 返回不能取消，原代码就是这样的
			return CancelTaskResponse.buildTaskNotCancelableError(request.getId());
		} finally {
			lock.unlock();  // 释放锁
		}
	}

	/**
	 * 设置任务通知配置
	 *
	 * @param request
	 * @return
	 */
	@Override
	public SetTaskPushNotificationResponse onSetTaskPushNotification(SetTaskPushNotificationRequest request) {
		log.info("Setting task push notification {}", request.getParams().getId());
		TaskPushNotificationConfig taskPushNotificationConfig = request.getParams();
		try {
			this.setPushNotificationInfo(taskPushNotificationConfig.getId(), taskPushNotificationConfig.getPushNotificationConfig());
			return SetTaskPushNotificationResponse.buildResponse(request.getId(), taskPushNotificationConfig);
		} catch (Exception e) {
			return SetTaskPushNotificationResponse.buildInternalError(request.getId());
		}
	}

	/**
	 * 获取订阅通知配置
	 * @param request
	 * @return
	 */
	@Override
	public GetTaskPushNotificationResponse onGetTaskPushNotification(GetTaskPushNotificationRequest request) {
		log.info("Getting task push notification {}", request.getParams().getId());
		TaskIdParams taskParams = request.getParams();
		try {
			Common.PushNotificationConfig pushNotificationConfig = this.getPushNotificationInfoForStore(taskParams.getId()).get();
			return GetTaskPushNotificationResponse.buildResponse(request.getId(),
					TaskPushNotificationConfig.builder()
							.id(taskParams.getId())
							.pushNotificationConfig(pushNotificationConfig).build()
			);
		} catch (Exception e) {
			return GetTaskPushNotificationResponse.buildInternalError(request.getId());
		}
	}

	/**
	 * 重新订阅任务，还未实现
	 *
	 * @param request
	 * @return
	 */
	@Override
	public Flux<SendTaskResponse> onResubscribeToTask(TaskResubscriptionRequest request) {
		return Flux.just(SendTaskResponse.buildUnsupportedOperationError(request.getId()));
	}


	/**
	 * 同步请求
	 * hostAgent 拿到该结果，会转换成use角色再取请求获取确认信息
	 * @param request
	 * @return
	 */
	@Override
	public JSONRPCMessage onSendTask(SendTaskRequest request) {
		log.info("request task {}", request.getParams().getId());
		// 验证请求
		Optional<JSONRPCResponse> optionalError = this.validateRequest(request.getParams(), request.getId());
		if (optionalError.isPresent()) {
			return optionalError.get();
		}
		// 更新插入任务
		this.upsertTask(request.getParams());

		// 验证设置通知配置
		var params = request.getParams();

		// 更新任务状态
		Task task = this.updateTask(
				params.getId(),
				Common.TaskStatus.builder().state(TaskState.WORKING).build(),
				Lists.newArrayList()
		);

		// 任务发起智能体推送通知配置存在并且该智能支持推动
		if (params.getPushNotification() != null && this.a2aServerProperties.getCapabilities().pushNotifications()) {
			//设置推送配置信息，并且验证消息服务器是否可用
			if (!this.setPushNotificationInfo(params.getId(), params.getPushNotification())) {
				return SendTaskResponse.buildInvalidParamsError(request.getId(), new InvalidParamsError("Push notification URL is invalid"));
			}
			// 发送通知消息，发送工作中
			this.sendTaskNotification(task);
		}

		String query = PartUtils.getTextContent(params.getMessage());

		try {
			// 智能体调用
			AgentGeneralResponse result = this.agentService.call(AgentRequest.builder()
					.text(query)
					.sessionId(params.getSessionId())
					.parts(params.getMessage().getParts())
					.build());

			return this.processAgentResponse(request, result);
		} catch (Exception e) {
			log.error("Error invoking agent: ", e.getMessage());
			throw new RuntimeException("Error invoking agent: " + e.getMessage());
		}
	}

	/**
	 * 任务工作中状态更新
	 * @param requestId
	 * @param params
	 * @param fluxSink
	 */
	private void fluxSendWorkingEvent(String requestId, TaskSendParams params, FluxSink fluxSink) {
		Common.TaskStatus taskStatus =
				Common.TaskStatus.builder().state(TaskState.WORKING).build();
		// 更新事件
		var taskUpdateEvent = TaskStatusUpdateEvent.builder()
				.status(taskStatus)
				.id(params.getId())
				.done(Boolean.FALSE)
				.build();

		fluxSink.next(SendTaskStreamingResponse.buildResponse(requestId, taskUpdateEvent));
	}

	/**
	 * 发送任务流模式，留给具体实现方法去实现
	 * hostAgent 拿到具体智能体的响应内容 AgentGeneralResponse 是我们自己智能体的交换数据的响应内容格式，最后会转换成A2A协议的格式内容返回给智能体
	 * @param request
	 * @return
	 */
	@Override
	public Flux<JSONRPCMessage> onSendTaskSubscribe(SendTaskStreamingRequest request) {
		Optional<JSONRPCResponse> optionalError = this.validateRequest(request.getParams(), request.getId());
		if (optionalError.isPresent()) {
			return Flux.just(optionalError.get());
		}
		// 保存任务, 任务状态是提交中
		this.upsertTask(request.getParams());

		// 验证设置通知配置
		TaskSendParams params = request.getParams();
		if (params.getPushNotification() != null && this.a2aServerProperties.getCapabilities().pushNotifications()) {
			if (!this.setPushNotificationInfo(params.getId(), params.getPushNotification())) {
				// 通知报错，则返回了异常
				return Flux.just(SendTaskResponse.buildInvalidParamsError(request.getId(), new InvalidParamsError("Push notification URL is invalid")));
			}
		}
		String query = PartUtils.getTextContent(params.getMessage());
		return Flux.create(fluxSink -> {

			ExecutorService executor = Executors.newSingleThreadExecutor();

			executor.submit(() -> {
				try {
					this.fluxSendWorkingEvent(request.getId(), params, fluxSink);

					StringBuffer buffer = new StringBuffer();

					Flux<AgentGeneralResponse> flux = this.agentService.stream(
							AgentRequest.builder()
									.text(query)
									.sessionId(params.getSessionId())
									.parts(params.getMessage().getParts())
									.build());

					AtomicReference<AgentResponseStatus> agentResponseStatus = new AtomicReference<>();
					flux.subscribe(
							agentResponse -> {
								try {
									buffer.append(agentResponse.getPart());
									agentResponseStatus.set(agentResponse.getStatus());
								} catch (JsonProcessingException e) {
									throw new RuntimeException(e);
								}
                                /*
                                不需要实时提交内容
                                CommonModel.TaskStatus taskStatus = this.buildTaskStatus(
                                        params.getId(),
                                        buffer.toString(),
                                        Lists.newArrayList(),
                                        AgentResponseStatus.WORKING
                                );

                                // 更新事件
                                var taskUpdateEvent = TaskStatusUpdateEvent.builder()
                                        .status(taskStatus)
                                        .id(params.getId())
                                        .done(Boolean.FALSE)
                                        .build();

                                fluxSink.next(SendTaskStreamingResponse.buildResponse(params.getId(), taskUpdateEvent));
                                 */
							},
							err -> {
								log.error("出现错误：{}", err.getMessage());
								fluxSink.next(
										SendTaskStreamingResponse.buildErrorResponse(
												request.getId(),
												new InternalA2aError("An error occurred while streaming the response: " + err.getMessage())
										)
								);
								fluxSink.complete();

							}, () -> {
								// log.info("执行完成");
								AgentGeneralResponse agentGeneralResponse = AgentGeneralResponse.fromText(buffer.toString(), agentResponseStatus.get());

								if (agentGeneralResponse != null) {
									List<Common.Artifact> artifacts = Lists.newArrayList();
									// 获取content 里面的内容
									Common.TaskStatus taskStatus = this.buildTaskStatus(
											params.getId(),
											agentGeneralResponse,
											artifacts
									);

									// 发送工件更新
									artifacts.forEach(artifact -> {
										TaskArtifactUpdateEvent taskArtifactUpdateEvent = TaskArtifactUpdateEvent.builder()
												.id(params.getId())
												.artifact(artifact)
												.build();
										fluxSink.next(SendTaskStreamingResponse.buildResponse(request.getId(), taskArtifactUpdateEvent));
									});
									var taskStatusUpdateEvent = TaskStatusUpdateEvent.builder()
											.status(taskStatus)
											.id(params.getId())
											.done(Boolean.TRUE)
											.build();

									fluxSink.next(SendTaskStreamingResponse.buildResponse(request.getId(), taskStatusUpdateEvent));
								}else{
									log.error("智能体未按照要求返回");
									fluxSink.next(
											SendTaskStreamingResponse.buildErrorResponse(
													request.getId(),
													new InternalA2aError("智能体未按照要求返回"))
									);
								}
								fluxSink.complete();
							});
				} catch (Exception e) {
					fluxSink.error(e); // 处理异常
					log.error("处理异常");
				} finally {
					executor.shutdown(); // 关闭线程池
				}
			});

		});
	}
}
