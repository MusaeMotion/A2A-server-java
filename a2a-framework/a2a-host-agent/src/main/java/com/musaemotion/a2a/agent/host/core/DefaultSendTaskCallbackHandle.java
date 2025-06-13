package com.musaemotion.a2a.agent.host.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.musaemotion.a2a.agent.host.manager.AbstractTaskCenterManager;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.common.constant.MessageRole;
import com.musaemotion.a2a.common.constant.TaskState;
import com.musaemotion.a2a.common.event.TaskArtifactUpdateEvent;
import com.musaemotion.a2a.common.event.TaskEvent;
import com.musaemotion.a2a.common.event.TaskStatusUpdateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static com.musaemotion.a2a.common.constant.MetaDataKey.CONVERSATION_ID;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/10  12:50
 * @description 智能体任务发送回调默认处理
 */
@Slf4j
public class DefaultSendTaskCallbackHandle implements SendTaskCallbackHandle {

	/**
	 * 智能体任务管理器
	 */
	private AbstractTaskCenterManager taskCenterManager;

	public DefaultSendTaskCallbackHandle(AbstractTaskCenterManager taskCenterManager) {
		this.taskCenterManager = taskCenterManager;
	}

	/**
	 *
	 * @param newTask
	 */
	@Override
	public void sendTaskCallback(Task newTask) {


		// 添加消息栈关系，消息之间前后关系
		this.insertIdTrace(newTask.getStatus().getMessage());

		Optional<Task> optionalTask = this.taskCenterManager.getById(newTask.getId());
		if (optionalTask.isPresent()) {
			Task oldTask = optionalTask.get();
			oldTask.getStatus().setState(newTask.getStatus().getState());
			// 可以合并一些getMetadata 内容
			// oldTask.getStatus().getMessage().getMetadata().putAll(newTask.getStatus().getMessage().getMetadata());
			// oldTask.setStatus(newTask.getStatus());
			if(newTask.getArtifacts()!=null) {
				// 返回多个产出结果工件
				newTask.getArtifacts().forEach(artifact -> {
					this.processArtifactEvent(oldTask, artifact);
				});
			}
			this.insertMessageHistory(oldTask, newTask.getStatus().getMessage());
			this.taskCenterManager.updateTask(oldTask);
			return;
		}
		this.taskCenterManager.addTask(newTask);
	}


	/**
	 * 更新任务状态
	 * @param taskStatusUpdateEvent
	 */
	@Override
	public void sendTaskCallback(TaskStatusUpdateEvent taskStatusUpdateEvent) {

		Task oleTask = this.addOrGetTask(taskStatusUpdateEvent);
        // 这里会通知完成
		oleTask.setStatus(taskStatusUpdateEvent.getStatus());

		// 添加消息栈关系，消息之间前后关系
		this.insertIdTrace(taskStatusUpdateEvent.getStatus().getMessage());
		// 给当前任务添加历史记录
		this.insertMessageHistory(oleTask, taskStatusUpdateEvent.getStatus().getMessage());

		this.taskCenterManager.updateTask(oleTask);
	}

	/**
	 * 完成任务状态更新
	 * @param taskArtifactUpdateEvent
	 */
	@Override
	public void sendTaskCallback(TaskArtifactUpdateEvent taskArtifactUpdateEvent) {

		Task oldTask = this.addOrGetTask(taskArtifactUpdateEvent);

		this.processArtifactEvent(oldTask, taskArtifactUpdateEvent.getArtifact());

		this.taskCenterManager.updateTask(oldTask);
	}


	/**
	 * 消息栈
	 * @param message
	 */
	private void insertIdTrace(Common.Message message) {
		if (message == null) {
			return;
		}
		String messageId = message.getMessageId();
		String lastMessageId = message.getLastMessageId();
		if (messageId != null && lastMessageId != null) {
			// 建立上一条和下一条的关系
			this.taskCenterManager.getNextId().put(lastMessageId, messageId);
		}
	}

	/**
	 * 状态更新，有可能是初始化创建
	 * @param input
	 * @return
	 */
	private Task addOrGetTask(TaskEvent input) {
		Optional<Task> optionalTask = this.taskCenterManager.getById(input.getId());
		if (optionalTask.isEmpty()) {
			String conversationId = null;
			if (input.getMetadata() != null && input.getMetadata().containsKey(CONVERSATION_ID)) {
				conversationId = input.getMetadata().get(CONVERSATION_ID).toString();
			}
			Task newTask = Task.builder().id(input.getId())
					.status(Common.TaskStatus.builder().state(TaskState.SUBMITTED).build())
					.metadata(input.getMetadata())
					.artifacts(Lists.newArrayList())
					.sessionId(conversationId)
					.build();
			this.taskCenterManager.addTask(newTask);
			return newTask;
		}

		return optionalTask.get();
	}

	/**
	 * 插入聊天记录
	 * @param oldTask
	 * @param message
	 */
	private void insertMessageHistory(Task oldTask, Common.Message message) {
		if (message == null || message.getMessageId() == null) {
			return;
		}
		if (oldTask.getHistory() == null) {
			oldTask.setHistory(new ArrayList<>());
		}
		// 状态消息的消息id
		String newMessageId = message.getMessageId();
		if (!oldTask.getHistory().stream().anyMatch(historicalMessage -> historicalMessage.getMessageId().equals(newMessageId))) {
			// TaskState.COMPLETED 角色为空，是消息完成消息
			if(message.getRole() == null) {
				message = Common.Message.newMessage(MessageRole.AGENT, Lists.newArrayList(new Common.TextPart("完成请求更新状态")), Maps.newHashMap());
			}
			oldTask.getHistory().add(message);
		} else {
			log.info("Message id already in history: {} , History: {}", newMessageId, oldTask.getHistory());
		}
	}

	/**
	 * 处理工件, 后续调整到任务中心去取对应工件信息
	 * @param currentTask
	 * @param artifact
	 */
	private void processArtifactEvent(Task currentTask, Common.Artifact artifact) {
		if (!artifact.getAppend()) {
			if (artifact.getLastChunk() || artifact.getLastChunk() == null) {
				if (currentTask.getArtifacts() == null) {
					currentTask.setArtifacts(new ArrayList<>());
				}
				currentTask.getArtifacts().add(artifact);
			} else {
				if (!this.taskCenterManager.getArtifactChunks().containsKey(currentTask.getId())) {
					Map<Integer, Common.Artifact> artifactMap = Maps.newHashMap();
					this.taskCenterManager.getArtifactChunks().put(
							currentTask.getId(),
							artifactMap
					);
				}
				Map<Integer, Common.Artifact> artifactMap = this.taskCenterManager.getArtifactChunks().get(currentTask.getId());
				artifactMap.put(artifact.getIndex(), artifact);
			}
		} else {
			Map<Integer,  Common.Artifact> tempArtifacts =  this.taskCenterManager.getArtifactChunks().get(currentTask.getId());
			if (tempArtifacts != null && tempArtifacts.containsKey(artifact.getIndex())) {
				Common.Artifact currentTempArtifact = tempArtifacts.get(artifact.getIndex());
				currentTempArtifact.getParts().addAll(artifact.getParts());
				if (artifact.getLastChunk()) {
					currentTask.getArtifacts().add(currentTempArtifact);
					tempArtifacts.remove(artifact.getIndex());
				}
			}
		}
	}
}
