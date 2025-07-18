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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.musaemotion.a2a.agent.client.server.PushNotificationServer;
import com.musaemotion.a2a.agent.host.ext.A2AToolCallingManager;
import com.musaemotion.a2a.agent.host.manager.AbstractMessageManager;
import com.musaemotion.a2a.agent.host.manager.AbstractRemoteAgentManager;
import com.musaemotion.a2a.agent.host.manager.AbstractTaskCenterManager;
import com.musaemotion.a2a.agent.host.model.AgentSkillVo;
import com.musaemotion.a2a.agent.host.model.RemoteAgentInfo;
import com.musaemotion.a2a.agent.host.provider.ChatModelProvider;
import com.musaemotion.a2a.common.AgentCard;
import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.FileBlob;
import com.musaemotion.a2a.common.base.FilePart;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.common.constant.MediaType;
import com.musaemotion.a2a.common.constant.MessageRole;
import com.musaemotion.a2a.common.constant.TaskState;
import com.musaemotion.a2a.common.request.params.TaskSendParams;
import com.musaemotion.a2a.common.utils.GuidUtils;
import com.musaemotion.a2a.common.utils.PartUtils;
import com.musaemotion.agent.BasisAgent;
import com.musaemotion.agent.AgentPromptProvider;
import com.musaemotion.agent.model.FileInfo;
import com.musaemotion.agent.model.SendMessageRequest;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.util.Assert;
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
 * @package：com.musaemotion.a2a.agent.client
 * @project：A2A
 * @date：2025/4/28 10:03
 * @description：请完善描述
 */
@Slf4j
public class HostAgent {

	/**
	 * 远程智能体地址, 需要使用先必须加载到远程地址当中
	 */
	private List<String> remoteAgentAddresses;

	/**
	 * 任务状态的回调, 可以用于落库与打印监控
	 */
	private SendTaskCallbackHandle callback;

	/**
	 * 通知服务
	 */
	private PushNotificationServer pushNotificationServer;

	/**
	 * 智能体
	 */
	private BasisAgent basisAgent;

	/**
	 * spring ai chatModel
	 */
	private ChatModelProvider chatModelProvider;

	/**
	 * Agent Card 管理服务
	 */
	private AbstractRemoteAgentManager remoteAgentManager;

	/**
	 * 任务状态
	 */
	private AbstractTaskCenterManager taskCenterManager;

	/**
	 * 获取
	 */
	private AbstractMessageManager messageManager;

	/**
	 * 可观察性
	 */
	private ObservationRegistry observationRegistry;

	/**
	 * 远程智能体服务
	 */
	private AgentPromptProvider agentPromptProvider;

	/**
	 * 聊天记录
	 */
	private ChatMemoryRepository chatMemoryRepository;
	/**
	 * 构造
	 * @param remoteAgentAddresses
	 * @param callback
	 * @param pushNotificationServer
	 * @param chatModelProvider
	 */
	public HostAgent(List<String> remoteAgentAddresses, SendTaskCallbackHandle callback, PushNotificationServer pushNotificationServer, ChatModelProvider chatModelProvider, AbstractRemoteAgentManager remoteAgentManager, AbstractTaskCenterManager taskCenterManager, AbstractMessageManager messageManager, AgentPromptProvider agentPromptProvider, ObservationRegistry observationRegistry, ChatMemoryRepository chatMemoryRepository) {
		this.remoteAgentAddresses = remoteAgentAddresses;
		this.callback = callback;
		this.remoteAgentManager = remoteAgentManager;
		this.pushNotificationServer = pushNotificationServer;
		this.chatModelProvider = chatModelProvider;
		this.taskCenterManager = taskCenterManager;
		this.messageManager = messageManager;
		this.observationRegistry = observationRegistry;
		this.agentPromptProvider = agentPromptProvider;
		this.chatMemoryRepository = chatMemoryRepository;
		this.initHostAgent();

	}

	/**
	 * 创建host智能体
	 */
	private void createHostAgent() {
		Assert.notNull(this.chatModelProvider, "chatModelProvider must not be null");

		ToolCallback[] toolCallbacks = ToolCallbacks.from(this);
		this.basisAgent = BasisAgent.builder()
				.id("host-agent")
				.name("host-agent")
				.description("""
                        This agent orchestrates the decomposition of the user request into
                        tasks that can be performed by the child agents.
                        """)
				// 读取两条记忆
				.chatMemorySize(5)
				.chatMemoryRepository(this.chatMemoryRepository)
				.chatClient(ChatClient.create(this.chatModelProvider.getChatModel(), this.observationRegistry))
				.observationRegistry(this.observationRegistry)
				.agentPromptProvider(this.agentPromptProvider)
				.toolCallbacks(Arrays.stream(toolCallbacks).toList())
				.build();
	}


	/**
	 * 刷新所有远程智能体配置
	 */
	private void initRegistrationAuthority() {
		//  启动时默认配置的远程智能体，自动注册智能体
		this.remoteAgentAddresses.forEach(agentUrl -> {
			this.remoteAgentManager.registerAgent(agentUrl);
		});
		// 刷新已经存在的智能体进行连接
		List<AgentCard> agentCards = this.remoteAgentManager.refreshRemoteAgentConnections();
		// 注册通知
		this.registerNotification(agentCards);
	}

	/**
	 * 文件转map
	 * @param filePart
	 * @return
	 */
	private static Map<String, String> filePartToMap(Common.FilePart filePart) {
		Map<String, String> fileMap = new HashMap<>();
		if (filePart.getFile().getBytes() != null) {
			// TODO 这里请根据fileId生成文件异步保存
			// TODO 如果担心重复，重新生成filedId 然后重写filePart 的id也可以
			String fileId = filePart.getFile().getName();
			Base64.Decoder decoder = Base64.getDecoder();
			byte[] fileBytes = decoder.decode(filePart.getFile().getBytes());
			FilePart fileBytePart = new FilePart(new FileBlob(filePart.getFile().getMimeType(), fileBytes));
			log.warn("保存图片文件，到时候用url 访问 fileBytePart: {}", fileBytePart);
			fileMap.put(ARTIFACT_FILE_ID, fileId);
		}
		if (StringUtils.hasText(filePart.getFile().getUri())) {
			fileMap.put(ARTIFACT_FILE_URL, filePart.getFile().getUri());
		}
		fileMap.put(ARTIFACT_MIME_TYPE, filePart.getFile().getMimeType());
		return fileMap;
	}

	/**
	 * 合并响应的Parts 内容
	 * @param parts
	 * @return
	 */
	private List<Object> convertParts(List<Common.Part> parts) {
		List<Object> result = new ArrayList<>();
		if(!CollectionUtils.isEmpty(parts)) {
			parts.stream().forEach(part -> {
				result.add(this.convertPart(part));
			});
		}
		return result;
	}
	/**
	 * 转换类型
	 * @param part
	 * @return
	 */
	private Object convertPart(Common.Part part) {
		if (part instanceof Common.TextPart textPart) {
			return textPart.getText();
		}
		if (part instanceof Common.DataPart dataPart) {
			return dataPart.getData();
		}
		if (part instanceof Common.FilePart filePart) {
			return filePartToMap(filePart);
		}
		return "Unknown type: " + part.getClass();
	}


	/**
	 * 初始化主机智能体
	 */
	public void initHostAgent() {
		// 创建
		this.createHostAgent();
		// 初始化远程智能体中心
		this.initRegistrationAuthority();
	}

	/**
	 * 同步调用
	 * @param input
	 * @param toolContext
	 * @param files
	 * @return
	 */
	public ChatResponse call(SendMessageRequest input,  Map<String, Object> toolContext, List<FileInfo> files) {
		return this.basisAgent.call(input, toolContext, files);
	}

	/**
	 * 流调用
	 * @param input
	 * @param toolContext
	 * @param files
	 * @return
	 */
	public Flux<ChatResponse> stream(SendMessageRequest input,  Map<String, Object> toolContext, List<FileInfo> files) {
		return this.basisAgent.stream(input, toolContext, files);
	}




	/**
	 * @Tool 不是必须的，但是加载注解，代码可读性更高
	 * 获取远程代码列表
	 * @return
	 */
	@Tool(description = "列出可用于委派任务可用的 remote agent")
	public String listRemoteAgents() {
		String remoteAgents = this.loadRemoteAgentsToString();
	    // log.error("remoteAgents:{}", remoteAgents);
		return remoteAgents;
	}

	/**
	 *  @Tool 不是必须的，但是加载注解，代码可读性更高
	 * 发送任务
	 * @param agentName
	 * @param message
	 * @param toolContext
	 * @return
	 */
	@Tool(description = """
		Sends a task either streaming (if supported) or non-streaming.
	
		This will send a message to the remote agent named agent_name.
	
		Args:
		  agentName: The name of the agent to send the task to.
		  message: The message to send to the agent for the task.
	
		Yields:
		  A dictionary of JSON data.
	""")
	public String sendTask(String agentName, String message, ToolContext toolContext) throws Throwable {
		// 获取当前调度的智能体连接对象
		A2aRemoteAgentConnections client = (A2aRemoteAgentConnections) this.remoteAgentManager.getRemoteAgentConnections(agentName)
				.orElseThrow(() -> new RuntimeException("Agent " + agentName + " not found"));
		// 判断智能体连接是否存在
		if (client == null) {
			throw new IllegalArgumentException("Client not available for " + agentName);
		}
		// 获取工具上下文信息里面的状态信息
		Map<String, Object> state = (Map<String, Object>)toolContext.getContext().get(STATE);

		var request = this.sendBefore(state, agentName, message);

		Task result = client.sendTask(request, this.callback);

		var response = this.sendAfter(request, result, state,  agentName);
        log.info("sendTask:{}", response.toString());
		return response;
	}

	/**
	 * 发送前
	 * @param state
	 * @param agentName
	 * @param message
	 * @return
	 */
	private TaskSendParams sendBefore(Map<String, Object> state, String agentName, String message){

		// 创建默认任务id
		String taskId = GuidUtils.createShortRandomGuid();
		String mainTaskId =GuidUtils.createGuid();
		if (state.containsKey(MAIN_TASK_ID)) {
			// 如果状态信息里包含任务id则使用任务id, 后续重发任务使用
			mainTaskId = (String) state.get(MAIN_TASK_ID);
		}
		// 写入状态上下当前智能体
		state.put(CUR_AGENT_NAME, agentName);
		// 当前智能体是否激活
		state.put(SESSION_ACTIVE, Boolean.TRUE);

		// 主任务Id
		state.put(MAIN_TASK_ID, mainTaskId);

		// 获取当前会话id=>交谈id
		String conversationId = (String) state.get(CONVERSATION_ID);
		// 创建消息id
		String messageId = GuidUtils.createShortRandomGuid();

		// 消息元数据
		Map<String, Object> messageMetadata = new HashMap<>();

		Map<String, Object> inputMetadata = (Map<String, Object>) state.get(INPUT_MESSAGE_METADATA);
		messageMetadata.putAll(inputMetadata);
		messageMetadata.put(INPUT_MESSAGE_ID, inputMetadata.get(MESSAGE_ID));
		messageMetadata.put(CONVERSATION_ID, conversationId);
		messageMetadata.put(MESSAGE_ID, messageId);


		// 创建发送给远程智能体的消息内容。
		List<Common.Part> parts = Lists.newArrayList();
		parts.add(new Common.TextPart(message));
		parts.addAll(this.loadSendParts(state, messageMetadata));

		Map<String, Object> sendMetadata = new HashMap<>();
		sendMetadata.put(CUR_AGENT_NAME, agentName);
		sendMetadata.put(MAIN_TASK_ID, mainTaskId);
		sendMetadata.putAll(messageMetadata);
		// 构建远程智能体 A2A 请求体
		return TaskSendParams.newUserTextInstance(
				taskId,
				conversationId,
				Common.Message.newMessage(MessageRole.AGENT, parts, messageMetadata),
				Arrays.asList(MediaType.TEXT, MediaType.TEXT_PLAIN, MediaType.IMAGE_PNG),
				sendMetadata,
				// 如果通知服务启动，设置通知服务的地址
				pushNotificationServer ==null?"": pushNotificationServer.getExternalUrl()
		);
	}

	/**
	 * 扩展处理
	 * @param responseTask 返回的 task
	 */
	private void sendAfterPart(Task responseTask, List<Common.Part> parts) {

		Common.TaskStatus resultTaskStatus = responseTask.getStatus();

		Task curTask = this.taskCenterManager.getById(responseTask.getId()).get();
		curTask.getStatus().setState(resultTaskStatus.getState());

		List<Common.Artifact> artifacts = Lists.newArrayList();
		artifacts.add(Common.Artifact.builder().lastChunk(Boolean.TRUE).parts(Lists.newArrayList()).build());
		Common.Artifact artifact = artifacts.get(0);

		if (!CollectionUtils.isEmpty(parts)) {
			artifact.getParts().addAll(parts);
		}

		if (!CollectionUtils.isEmpty(responseTask.getArtifacts())) {
			artifact.getParts().addAll(
					responseTask.getArtifacts().stream().map(item->item.getParts()).flatMap(Collection::stream)
							.collect(Collectors.toUnmodifiableList())
			);
		}

		curTask.setArtifacts(artifacts);
		responseTask.setArtifacts(artifacts);
		curTask.mergeMetaData(responseTask.getMetadata());
		this.taskCenterManager.updateTask(curTask);
	}

	/**
	 * 消息发送后处理
	 * @param request
	 * @param responseTask
	 * @param state
	 * @param agentName
	 * @return
	 */
	private String sendAfter(TaskSendParams request, Task responseTask, Map<String, Object> state, String agentName) throws JsonProcessingException {
		String messageId = request.getMessage().getMessageId();
		if(responseTask == null){
			Optional<Task> opTask = this.taskCenterManager.getById(request.getId());
			responseTask = opTask.get();
			responseTask.setStatus(Common.TaskStatus.builder().state(TaskState.FAILED).build());
		}
		// 以下是处理远程智能体响应内容
		// 判断远程智能体响应内容运行状态, 激活状态，只写入正常工作中的状态 SUBMITTED WORKING INPUT_REQUIRED，如果不是这三个状态，激活状态则是 false
		// 如果是这三个状态则是true
		state.put(SESSION_ACTIVE, !Arrays.asList(
				TaskState.COMPLETED,
				TaskState.CANCELED,
				TaskState.FAILED,
				TaskState.UNKNOWN
		).contains(responseTask.getStatus().getState()));

		// 远程智能体执行任务的状态
		Common.TaskStatus resultTaskStatus = responseTask.getStatus();

		if (resultTaskStatus.getState() == TaskState.CANCELED) {
			throw new IllegalArgumentException("Agent " + agentName + " task " + responseTask.getId() + " is cancelled");
		}

		// 包装远程智能体响应的结果。
		List<Object> responsePart = new ArrayList<>();
		if (resultTaskStatus.getState().equals(TaskState.WORKING)) {
			// 如果任务 状态是 WORKING 或 INPUT_REQUIRED, 消息内容就在 task.getStatus().getMessage()里，远程智能响应作为状态的一部分返回 message 格式。
			responsePart.addAll(convertParts(resultTaskStatus.getMessage().getParts()));
		}
		if (resultTaskStatus.getState().equals(TaskState.INPUT_REQUIRED)) {
			state.put(A2AToolCallingManager.RETURN_DIRECT, true);
			responsePart.addAll(resultTaskStatus.getMessage().getParts());
			this.sendAfterPart(responseTask, resultTaskStatus.getMessage().getParts());
		}
		if (resultTaskStatus.getState().equals(TaskState.FAILED) ) {
			state.put(A2AToolCallingManager.RETURN_DIRECT, true);
			var part = new Common.TextPart("智能体 " + agentName + " 任务： " + responseTask.getId() + " 运行失败");
			responsePart.add(part);
			this.sendAfterPart(responseTask, Lists.newArrayList(part));
		}
		// 如果 task 状态 COMPLETED，则在生产工作件里的part包装数据
		if(resultTaskStatus.getState().equals(TaskState.COMPLETED)) {
			// 获取任务相关信息, 因为在远程智能体调用之前，和调用之后，都会有回调创建任务，所以这里能获取到任务相关信息
			Optional<Task> opTask = this.taskCenterManager.getById(responseTask.getId());
			Task task = opTask.orElse(null);
			if (task !=null && !CollectionUtils.isEmpty(task.getArtifacts())) {
				for (Common.Artifact artifact : task.getArtifacts()) {
					responsePart.addAll(convertParts(artifact.getParts()));
				}
			}
			// 把当前消息id 写入上一次消息
			state.put(LAST_MESSAGE_ID, messageId);
		}
		// 当前智能体执行完成
		state.put(SESSION_ACTIVE, Boolean.FALSE);

		log.info("当前运行智能体: {}", state.get(CUR_AGENT_NAME));

		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(responseTask);
	}

	/**
	 * 加载发送消息parts
	 * @param state
	 * @param messageMetadata
	 * @return
	 */
	private List<Common.Part> loadSendParts(Map<String, Object> state, Map<String, Object> messageMetadata) {
		List<Common.Part> parts = Lists.newArrayList();
		// 这种只适合工作流串行任务
		if (state.containsKey(LAST_MESSAGE_ID)) {
			String messageId = state.get(LAST_MESSAGE_ID).toString();
			Optional<Task> optional  = this.taskCenterManager.getByMessageId(messageId);
			if (optional.isPresent() && optional.get().getStatus().getState() == TaskState.COMPLETED) {
				Task task = optional.get();
				parts.addAll(
						PartUtils.getFilePartByParts(
								task.getArtifacts().stream().map(Common.Artifact::getParts).flatMap(Collection::stream).collect(Collectors.toList())
						)
				);
			}
			return parts;
		}
		// 从交谈消息里获取消息parts
		String inputMessageId = messageMetadata.get(INPUT_MESSAGE_ID).toString();
		Optional<Common.Message> optionalMessage = this.messageManager.getByMessageId(inputMessageId);
		if (optionalMessage.isPresent()) {
			parts.addAll(PartUtils.getFilePartByMessage(optionalMessage.get()));
		}
		return parts;
	}

	/**
	 * 注册通知
	 * @param agentCards
	 */
	public void registerNotification(List<AgentCard> agentCards) {
		if(this.pushNotificationServer != null){
			agentCards.forEach(agentCard -> {
				// 注册到通知服务器，需要 jwks 验证
				if(agentCard.getCapabilities().pushNotifications() ) {
					this.pushNotificationServer.registerAgent(agentCard.getName(), agentCard.getUrl());
				}
			});
		}
	}

	/**
	 * 获取当前智能体列表
	 * @return
	 */
	private List<RemoteAgentInfo> loadRemoteAgents() {
		List<AgentCard> agents = this.remoteAgentManager.listAll();
		if (CollectionUtils.isEmpty(agents)) {
			return Lists.newArrayList();
		}
		List<RemoteAgentInfo> remoteAgentInfos = Lists.newArrayList();
		agents.forEach(agentCard -> {
			remoteAgentInfos.add(
					RemoteAgentInfo.builder().agentName(agentCard.getName())
							.description(agentCard.getDescription())
							.agentSkills(AgentSkillVo.fromList(agentCard.getSkills()))
							.build()
			);
		});
		return remoteAgentInfos;
	}

	/**
	 * 获取字符串列表
	 * @return
	 */
	public String loadRemoteAgentsToString() {
		StringBuffer sb = new StringBuffer();
		ObjectMapper mapper = new ObjectMapper();
		this.loadRemoteAgents().forEach(agentInfo -> {
			try {
				sb.append(mapper.writeValueAsString(agentInfo)+"\n");
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
		return sb.toString();
	}

	/**
	 * 构建模式
	 * @return
	 */
	public static HostAgent.Builder builder() {
		return new HostAgent.Builder();
	}

	public static class Builder {

		private List<String> remoteAgentAddresses;

		private SendTaskCallbackHandle sendTaskCallback;

		private PushNotificationServer pushNotificationServer;

		private ChatModelProvider ChatModelProvider;

		private AbstractRemoteAgentManager remoteAgentManager;

		private AbstractTaskCenterManager taskCenterManager;

		private AbstractMessageManager messageManager;

		private ObservationRegistry observationRegistry;

		private AgentPromptProvider agentPromptProvider;

		private ChatMemoryRepository chatMemoryRepository;

		private Builder() {
		}

		public HostAgent.Builder remoteAgentAddresses(List<String> remoteAgentAddresses) {
			this.remoteAgentAddresses = remoteAgentAddresses;
			return this;
		}

		public HostAgent.Builder sendTaskCallback(SendTaskCallbackHandle sendTaskCallback) {
			this.sendTaskCallback = sendTaskCallback;
			return this;
		}

		public HostAgent.Builder pushNotificationServer(
				PushNotificationServer pushNotificationServer) {
			this.pushNotificationServer = pushNotificationServer;
			return this;
		}

		public HostAgent.Builder chatModelProvider(
				ChatModelProvider ChatModelProvider) {
			this.ChatModelProvider = ChatModelProvider;
			return this;
		}

		public HostAgent.Builder remoteAgentManager(
				AbstractRemoteAgentManager remoteAgentManager) {
			this.remoteAgentManager = remoteAgentManager;
			return this;
		}
		public HostAgent.Builder taskCenterManager(
				AbstractTaskCenterManager taskCenterManager) {
			this.taskCenterManager = taskCenterManager;
			return this;
		}
		public HostAgent.Builder messageManager(
				AbstractMessageManager messageManager) {
			this.messageManager = messageManager;
			return this;
		}
		public HostAgent.Builder observationRegistry(
				ObservationRegistry observationRegistry) {
			this.observationRegistry = observationRegistry;
			return this;
		}
		public HostAgent.Builder hostAgentPromptService(
				AgentPromptProvider agentPromptProvider) {
			this.agentPromptProvider = agentPromptProvider;
			return this;
		}

		public HostAgent.Builder chatMemoryRepository(
				ChatMemoryRepository chatMemoryRepository) {
			this.chatMemoryRepository = chatMemoryRepository;
			return this;
		}

		public HostAgent build() {
			Assert.notNull(ChatModelProvider, "chatModelProvider 不能为空");
			Assert.notNull(remoteAgentManager, "remoteAgentManager 不能为空");
			Assert.notNull(taskCenterManager, "taskCenterManager 不能为空");
			Assert.notNull(messageManager, "messageManager 不能为空");
			Assert.notNull(sendTaskCallback, "sendTaskCallback 不能为空");
			Assert.notNull(agentPromptProvider, "hostAgentPromptService 不能为空");
			Assert.notNull(this.chatMemoryRepository, "chatMemoryRepository 不能为空");

			HostAgent hostAgent = new HostAgent(remoteAgentAddresses, sendTaskCallback, pushNotificationServer, ChatModelProvider, remoteAgentManager, taskCenterManager, messageManager, agentPromptProvider, observationRegistry, chatMemoryRepository);
			return hostAgent;
		}

	}

}
