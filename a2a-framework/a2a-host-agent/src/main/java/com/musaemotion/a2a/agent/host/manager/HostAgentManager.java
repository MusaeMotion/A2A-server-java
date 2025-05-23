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

package com.musaemotion.a2a.agent.host.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.musaemotion.a2a.common.AgentCard;
import com.musaemotion.a2a.common.event.TaskArtifactUpdateEvent;
import com.musaemotion.a2a.common.event.TaskEvent;
import com.musaemotion.a2a.common.event.TaskStatusUpdateEvent;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.common.utils.JsonUtils;
import com.musaemotion.a2a.agent.host.core.HostAgent;
import com.musaemotion.a2a.agent.host.core.ISendTaskCallback;
import com.musaemotion.a2a.agent.host.model.response.CommonMessageExt;
import com.musaemotion.a2a.agent.host.model.response.PageInfo;
import com.musaemotion.a2a.agent.host.model.response.PageUtils;
import com.musaemotion.a2a.agent.host.model.response.SendMessageResponse;
import com.musaemotion.a2a.agent.host.model.service.Conversation;
import com.musaemotion.a2a.agent.host.model.service.RegisterAgentDto;
import com.musaemotion.a2a.agent.host.model.service.SearchRemoteAgentDto;
import com.musaemotion.a2a.agent.host.properties.A2aHostAgentProperties;
import com.musaemotion.a2a.common.constant.MessageRole;
import com.musaemotion.a2a.common.constant.TaskState;
import com.musaemotion.a2a.common.utils.GuidUtils;
import com.musaemotion.a2a.agent.client.server.PushNotificationServer;
import com.musaemotion.agent.HostAgentPromptService;
import com.musaemotion.agent.model.SendMessageRequest;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

import static com.musaemotion.a2a.common.constant.ArtifactDataKey.*;
import static com.musaemotion.a2a.common.constant.MetaDataKey.*;
import static com.musaemotion.agent.BasisAgent.STATE;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.service
 * @project：A2A
 * @date：2025/5/9 13:15
 * @description：请完善描述
 */
@Slf4j
@Service
public class HostAgentManager implements ISendTaskCallback {

    private AbstractConversationManager conversationManager;

    private AbstractMessageManager messageManager;

    private AbstractRemoteAgentManager remoteAgentManager;

    private HostAgent hostAgent;

    private PushNotificationServer pushNotificationServer;

    private AbstractTaskCenterManager taskCenterManager;

    /**
     * 可观察性
     */
    private ObservationRegistry observationRegistry;

    /**
     * host Agent 提示词service
     */
    private HostAgentPromptService hostAgentPromptService;

    /**
     *
     * @param chatModel
     * @param a2aHostAgentProperties
     * @param abstractConversationManager
     * @param abstractMessageManager
     * @param abstractRemoteAgentManager
     * @param pushNotificationServer
     */
    @Autowired
    public HostAgentManager(ChatModel chatModel, A2aHostAgentProperties a2aHostAgentProperties, AbstractConversationManager abstractConversationManager, AbstractMessageManager abstractMessageManager, AbstractRemoteAgentManager abstractRemoteAgentManager, AbstractTaskCenterManager abstractTaskCenterManager, HostAgentPromptService hostAgentPromptService, @Autowired(required = false)  PushNotificationServer pushNotificationServer,  @Autowired(required = false) ObservationRegistry observationRegistry) {
        this.conversationManager = abstractConversationManager;
        this.messageManager = abstractMessageManager;
        this.remoteAgentManager = abstractRemoteAgentManager;
        this.pushNotificationServer = pushNotificationServer;
        this.taskCenterManager = abstractTaskCenterManager;
        this.observationRegistry = observationRegistry;
        this.hostAgentPromptService = hostAgentPromptService;
        if (this.observationRegistry == null) {
            this.observationRegistry = ObservationRegistry.NOOP;
        }
        this.hostAgent = HostAgent.builder()
                .remoteAgentAddresses(
                        CollectionUtils.isEmpty(a2aHostAgentProperties.getRemoteAgentAddresses()) ?
                                Lists.newArrayList() : a2aHostAgentProperties.getRemoteAgentAddresses()
                )
                .pushNotificationServer(this.pushNotificationServer)
                .remoteAgentManager(this.remoteAgentManager)
                .taskCenterManager(this.taskCenterManager)
                .messageManager(this.messageManager)
                .observationRegistry(this.observationRegistry)
                .hostAgentPromptService(this.hostAgentPromptService)
                .chatModel(chatModel)
                .sendTaskCallback(this)
                .build();
        this.hostAgent.initHostAgent();
    }

    /**
     * 检查参数
     * @param input
     */
    private void checkSendMessageRequest(SendMessageRequest input) {
        if (StringUtils.isEmpty(input.getContent())) {
            throw new IllegalArgumentException("content is null");
        }
        if (StringUtils.isEmpty(input.getConversationId())) {
            throw new IllegalArgumentException("conversationId is null");
        }
        if (!this.conversationManager.exist(input.getConversationId())) {
            throw new IllegalArgumentException("conversation no exist for conversationId");
        }
    }


    /**
     * 发送之前处理消息
     * @param input
     * @return
     */
    private Common.Message sanitizeRequestMessage(SendMessageRequest input) {
        Boolean isEmpty = Optional.ofNullable(input.getMetadata()).map(m -> m.isEmpty()).orElse(true);
        Common.Message message = Common.Message.builder()
                .role(MessageRole.USER)
                .parts(input.getParams().getParts())
                .metadata(isEmpty ? Maps.newConcurrentMap() : input.getMetadata())
                .build();

        Optional<Common.Message> optionalMessage = this.messageManager.lastByConversationId(input.getConversationId());
        // 添加上一条消息
        if (optionalMessage.isPresent()) {
            message.getMetadata().put(LAST_MESSAGE_ID, optionalMessage.get().getMetadata().get(MESSAGE_ID));
        }
        return message;
    }

    /**
     * 处理响应消息
     * @param assistantMessage
     * @param userMessage
     * @return
     */
    private Common.Message sanitizeResponseMessage(AssistantMessage assistantMessage, Common.Message userMessage) {
        Common.Message message = null;
        if (JsonUtils.isJSON(assistantMessage.getText())) {
            message = Common.Message.builder()
                    .role(MessageRole.AGENT)
                    .metadata(assistantMessage.getMetadata())
                    .parts(Lists.newArrayList())
                    .build();
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(assistantMessage.getText());
                Common.Message finalMessage = message;
                jsonNode.forEach(json -> {
                    if (json.has(ARTIFACT_FILE_URL) || json.has(ARTIFACT_FILE_ID)) {
                        Common.FilePart filePart = new Common.FilePart();
                        filePart.setFile(Common.FileContent.builder()
                                .uri(json.get(ARTIFACT_FILE_URL) == null ? "" : json.get(ARTIFACT_FILE_URL).toString().replace("\"",""))
                                .name(json.get(ARTIFACT_FILE_ID) == null ? "" : json.get(ARTIFACT_FILE_ID).toString().replace("\"",""))
                                .mimeType(json.get(ARTIFACT_MIME_TYPE) == null ? "" : json.get(ARTIFACT_MIME_TYPE).toString().replace("\"",""))
                                .build());
                        finalMessage.getParts().add(filePart);
                    } else {
                        Common.DataPart dataPart = new Common.DataPart();
                        dataPart.setData(new ObjectMapper().convertValue(jsonNode, Map.class));
                        finalMessage.getParts().add(dataPart);
                    }
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException("响应内容反序列化失败");
            }
        } else {
            message = Common.Message.builder()
                    .role(MessageRole.AGENT)
                    .parts(Lists.newArrayList(new Common.TextPart(assistantMessage.getText())))
                    .metadata(assistantMessage.getMetadata())
                    .build();
        }
        message.getMetadata().put(LAST_MESSAGE_ID, userMessage.getMetadata().get(MESSAGE_ID).toString());
        message.getMetadata().put(CONVERSATION_ID, userMessage.getMetadata().get(CONVERSATION_ID).toString());
        message.getMetadata().put(MESSAGE_ID, GuidUtils.createShortRandomGuid());
        return message;
    }

    /**
     * 构建工具上下文
     * @param input
     * @return
     */
    private Map<String, Object> buildToolContext(SendMessageRequest input){
        Map<String, Object> state = new HashMap<>();
        state.put(INPUT_MESSAGE_METADATA, input.getMetadata());
        state.put(SESSION_ID, input.getConversationId());
        String lastMessageId = input.getLastMessageId();
        if(StringUtils.hasText(lastMessageId)){
           String taskId = this.taskCenterManager.getTaskMap().get(lastMessageId);
           if(StringUtils.hasText(taskId)){
               state.put(TASK_ID, taskId);
           }
        }
        Map<String, Object> toolContext = new HashMap<>();
        toolContext.put(STATE, state);
        return toolContext;
    }

    /**
     * 之前处理
     * @param input
     * @return
     */
    private Common.Message sendBefore(SendMessageRequest input){

        this.checkSendMessageRequest(input);

        // 消息处理
        Common.Message userMessage = this.sanitizeRequestMessage(input);

        // 消息管理器保存
        this.messageManager.add(userMessage);

        return userMessage;
    }

    /**
     * 之后处理
     * @param assistantMessage
     * @param userMessage
     * @return
     */
    private Common.Message sendAfter(AssistantMessage assistantMessage, Common.Message userMessage)  {

        Common.Message agnetMessage = this.sanitizeResponseMessage(assistantMessage, userMessage);

        // 消息管理器保存
        this.messageManager.add(agnetMessage);

        return agnetMessage;
    }

    /**
     * 同步请求消息
     * @param input
     * @return
     */
    public SendMessageResponse<CommonMessageExt> call(SendMessageRequest input) {

        Common.Message userMessage = this.sendBefore(input);

        AssistantMessage assistantMessage = this.hostAgent.call(input, this.buildToolContext(input));

        Common.Message agnetMessage = this.sendAfter(assistantMessage, userMessage);

		// 包装task列表
		var message = CommonMessageExt.fromMessage(agnetMessage);
		String lastMessageId = agnetMessage.getLastMessageId();
		if(StringUtils.hasText(lastMessageId)){
			List<Task> tasks = this.taskCenterManager.listByInputMessageId(Lists.newArrayList(agnetMessage.getLastMessageId()));
			message.setTask(tasks);
		}
		// 包装task列表

        return SendMessageResponse.buildMessageResponse(
				message,
                input.getConversationId()
        );
    }

    /**
     * 流请求
     * @param input
     * @return
     */
    public Flux<SendMessageResponse<Common.Message>> stream(SendMessageRequest input) {

        Common.Message userMessage = this.sendBefore(input);

        Flux<AssistantMessage> fluxAssistantMessage = this.hostAgent.stream(input, this.buildToolContext(input));

        Flux<SendMessageResponse<Common.Message>> flux = Flux.create(fluxSink -> {
            fluxAssistantMessage
                    .doFinally(i -> {
                        // log.error("HostAgent完成：{}", i.name());
                        fluxSink.complete();
                    })
                    .subscribe(assistantMessage -> {
                        Common.Message agnetMessage = this.sendAfter(assistantMessage, userMessage);
                        fluxSink.next(
                                SendMessageResponse.buildMessageResponse(
                                        agnetMessage,
                                        input.getConversationId()
                                )
                        );
                    });

        });
        return flux;
    }

    /**
     * 删除对话
     * @param conversationId
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(String conversationId) {
        this.conversationManager.delete(conversationId);
        this.messageManager.deleteByConversationId(conversationId);
        this.taskCenterManager.deleteByConversationId(conversationId);
    }

    /**
     * 创建对话
     * @return
     */
    public Conversation createConversation() {
        return this.conversationManager.create("");
    }

    /**
     * 获取对话列表
     * @return
     */
    public List<Conversation> listConversation() {
        return this.conversationManager.list();
    }

    /**
     * 获取对话相关的消息
     * @return
     */
    public List<CommonMessageExt> listMessage(String conversationId) {

        var messages = this.messageManager.listByConversationId(conversationId);
        List<CommonMessageExt> newMessages = messages.stream().map(item->CommonMessageExt.fromMessage(item)).collect(Collectors.toUnmodifiableList());

        List<String> messageIds = messages.stream()
                .filter(item->item.getLastMessageId()!=null)
                .map(Common.Message::getLastMessageId).distinct()
                .collect(Collectors.toUnmodifiableList());

        List<Task> tasks = this.taskCenterManager.listByInputMessageId(messageIds);
        newMessages.forEach(message->{
            var ts =  tasks.stream().filter(task -> task.getInputMessageId().equals(message.getLastMessageId())).collect(Collectors.toUnmodifiableList());
            if(!CollectionUtils.isEmpty(ts)){
                message.setTask(ts);
            }

        });

        return newMessages;
    }

    /**
     * 注册agent
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

    /**
     * Agent分页
     * @param searchInput
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<AgentCard> pageListAgent(SearchRemoteAgentDto searchInput, Integer pageNum, Integer pageSize) {
        pageNum = pageNum - 1;
        Page<AgentCard> page = this.remoteAgentManager.pageList(searchInput, pageNum, pageSize);
        PageInfo<AgentCard> pageInfo = PageUtils.springPageToMyPage(page);
        return pageInfo;
    }

    /**
     * 删除agent
     * @param ids
     * @return
     */
    public void deleteAgent(String ids) {
        if (!StringUtils.isEmpty(ids)) {
            List<String> id = Arrays.stream(ids.split(",")).collect(Collectors.toList());
            this.remoteAgentManager.delete(id);
        }
    }

    /**
     * 根据对话id获取列表
     * @param conversationId
     */
    public List<Task> listTaskByConversationId(String conversationId) {
        return this.taskCenterManager.getByConversationId(conversationId);
    }

    /**
     * 删除 task
     * @param ids
     * @return
     */
    public void deleteTask(String ids) {
        if (!StringUtils.isEmpty(ids)) {
            List<String> id = Arrays.stream(ids.split(",")).collect(Collectors.toList());
            this.taskCenterManager.removeTask(id);
        }
    }


    /**
     * 创建任务提交中回调
     * @param newTask
     */
    @Override
    public void sendTaskCallback(Task newTask) {
        // 建立消息和任务的关系
        this.attachMessageToTask(newTask.getStatus().getMessage(), newTask.getId());
        // 添加消息栈关系，消息之间前后关系
        this.insertIdTrace(newTask.getStatus().getMessage());
        Optional<Task> optionalTask = this.taskCenterManager.getById(newTask.getId());
        if (optionalTask.isPresent()) {
            Task oldTask = optionalTask.get();
            oldTask.setStatus(newTask.getStatus());
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

        oleTask.setStatus(taskStatusUpdateEvent.getStatus());
        // 建立消息和任务的关系
        this.attachMessageToTask(taskStatusUpdateEvent.getStatus().getMessage(), oleTask.getId());
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
     * 附加消息到任务，建立消息和任务的映射关系到map
     * @param message
     * @param taskId
     */
    private void attachMessageToTask(Common.Message message, String taskId) {
        if (message != null && message.getMetadata() != null && message.getMetadata().containsKey(MESSAGE_ID)) {
            String messageId = message.getMetadata().get(MESSAGE_ID).toString();
            this.taskCenterManager.getTaskMap().put(messageId, taskId);
        }
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
            log.warn("Message id already in history: {} , History: {}", newMessageId, oldTask.getHistory());
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
