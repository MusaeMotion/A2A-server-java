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
		Optional<Task> optionalTask = this.taskCenterManager.getById(newTask.getId());
		if (optionalTask.isPresent()) {
			Task oldTask = optionalTask.get();
			oldTask.getStatus().setState(newTask.getStatus().getState());
			if(newTask.getArtifacts()!=null) {
				// 返回多个产出结果工件
				newTask.getArtifacts().forEach(artifact -> {
					this.processArtifactEvent(oldTask, artifact);
				});
			}
			this.insertMessageHistory(oldTask, newTask.getStatus().getMessage());
			oldTask.mergeMetaData(newTask.getMetadata());
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

		Task oldTask = this.addOrGetTask(taskStatusUpdateEvent);

        // 更新状态
		oldTask.setStatus(taskStatusUpdateEvent.getStatus());

		// 给当前任务添加历史记录
		this.insertMessageHistory(oldTask, taskStatusUpdateEvent.getStatus().getMessage());

		this.taskCenterManager.updateTask(oldTask);
	}

	/**
	 * 完成任务状态更新
	 * @param taskArtifactUpdateEvent
	 */
	@Override
	public void sendTaskCallback(TaskArtifactUpdateEvent taskArtifactUpdateEvent) {

		Task oldTask = this.addOrGetTask(taskArtifactUpdateEvent);

		// 处理生产工件
		this.processArtifactEvent(oldTask, taskArtifactUpdateEvent.getArtifact());

		this.taskCenterManager.updateTask(oldTask);
	}


	/**
	 * 状态更新，有可能是初始化创建
	 * @param input
	 * @return
	 */
	private Task addOrGetTask(TaskEvent input) {
		Optional<Task> optionalTask = this.taskCenterManager.getById(input.getId());
		if (optionalTask.isEmpty()) {
			Task newTask = Task.buildSubmittedFrom(input);
			this.taskCenterManager.addTask(newTask);
			return newTask;
		}
		Task task = optionalTask.get();
		task.mergeMetaData(input.getMetadata());
		return task;
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
