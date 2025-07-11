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

package com.musaemotion.a2a.common.base;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.musaemotion.a2a.common.IMetadata;
import com.musaemotion.a2a.common.exception.A2AClientJSONException;
import com.musaemotion.a2a.common.constant.MessageRole;
import com.musaemotion.a2a.common.constant.TaskState;
import com.musaemotion.a2a.common.utils.MetadataUtils;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.musaemotion.a2a.common.constant.MetaDataKey.*;


/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：
 */
public class Common {


	/**
	 * 实际内容载体对象，文件、文本、数据。
	 */
	@Data
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({
			@JsonSubTypes.Type(value = TextPart.class, name = "text"),
			@JsonSubTypes.Type(value = FilePart.class, name = "file"),
			@JsonSubTypes.Type(value = DataPart.class, name = "data")
	})
	public static abstract class Part implements Serializable {
		/**
		 * 载体类型
		 */
		protected String type;

		/**
		 * 载体其他信息
		 */
		private Map<String, Object> metadata;

	}

	/**
	 * 消息文本部分
	 */
	@Data
	@EqualsAndHashCode(callSuper=true)
	public static class TextPart extends Part {
		// 文本内容
		private String text;

		public TextPart() {
			this.type = "text";
			this.text = "";
		}
		public TextPart(String text) {
			this.type = "text";
			this.text = text;
		}

		@Override
		public String toString() {
			return this.text;
		}
	}

	/**
	 * 消息文件部分
	 */
	@Data
	@EqualsAndHashCode(callSuper=true)
	public static class FilePart extends Part {
		// 文件内容
		private FileContent file;

		public FilePart() {
			this.type = "file";
		}
		public FilePart(FileContent file) {
			this.type = "file";
			this.file = file;
		}

		@Override
		public String toString() {
			return this.file.bytes;
		}

		public static FilePart newFilePart(FileContent file) {
			return new FilePart(file);
		}
	}

	/**
	 * 包含表单数据机构
	 * 比如
	 * {
	 *    'type': 'form',
	 *    'form': {
	 *        'type': 'object',
	 *        'properties': {
	 *              'name': {
	 *                  'type': 'string',
	 *                  'description': 'Enter your name',
	 *                  'title': 'Name',
	 *               },
	 *               'date': {
	 *                   'type': 'string',
	 *                   'format': 'date',
	 *                   'description': 'Birthday',
	 *                   'title': 'Birthday',
	 *               },
	 *        },
	 *       'required': ['date'],
	 *     },
	 *    'form_data': {
	 *        'name': 'John Smith',
	 *     },
	 *    'instructions': 'Please provide your birthday and name'
	 *  }
	 */
	@Data
	@EqualsAndHashCode(callSuper=true)
	public static class DataPart extends Part {
		// data 内容
		private Map<String, Object> data;

		public DataPart() {
			this.type = "data";
			this.data = new HashMap<>();
		}
		public DataPart(Map<String, Object> data) {
			this.type = "data";
			this.data = data;
		}
		@Override
		public String toString() {
			ObjectMapper mapper = new ObjectMapper();
			try {
				return mapper.writeValueAsString(this.data);
			} catch (JsonProcessingException e) {
				throw new A2AClientJSONException(e.getMessage());
			}
		}
	}


	/**
	 * 文件内容
	 */
	@Data
	@NoArgsConstructor
	@Builder
	public static class FileContent  implements Serializable {
		private String name;
		private String mimeType;
		private String bytes;
		private String uri;

		public FileContent(String name, String mimeType, String bytes, String uri) {
			this.name = name;
			this.mimeType = mimeType;
			this.bytes = bytes;
			this.uri = uri;
			validateContent();
		}
		@JsonIgnore
		private void validateContent() {
			if ((bytes == null || bytes.isEmpty()) && (uri == null || uri.isEmpty())) {
				throw new IllegalArgumentException("Either 'bytes' or 'uri' must be present in the file data");
			}
			if (bytes != null && !bytes.isEmpty() && uri != null && !uri.isEmpty()) {
				throw new IllegalArgumentException("Only one of 'bytes' or 'uri' can be present in the file data");
			}
		}
	}

	/**
	 * 消息格式
	 */
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Message implements Serializable, IMetadata {
		private MessageRole role;
		private List<Part> parts;
		private Map<String, Object> metadata;
		@JsonIgnore
		public String getLastMessageId(){
			if(metadata==null){
				return null;
			}
			if(!metadata.containsKey(LAST_MESSAGE_ID)){
				return null;
			}
			return metadata.get(LAST_MESSAGE_ID).toString();
		}
		@JsonIgnore
		public String getMessageId(){
			if(metadata==null){
				return null;
			}
			if(!metadata.containsKey(MESSAGE_ID)){
				return null;
			}
			return metadata.get(MESSAGE_ID).toString();
		}
		@JsonIgnore
		public String getConversationId(){
			if(metadata==null){
				return null;
			}
			if(!metadata.containsKey(CONVERSATION_ID)){
				return null;
			}
			return metadata.get(CONVERSATION_ID).toString();
		}

		/**
		 *
		 * @param messageRole
		 * @param parts
		 * @param metadata
		 * @return
		 */
		public static Message newMessage(MessageRole messageRole, List<Part> parts, Map<String, Object> metadata) {
			Message message = new Message();
			message.setRole(messageRole);
			message.setParts(parts);
			message.setMetadata(metadata);
			return message;
		}

		/**
		 * 构建使用Token
		 * @param usageTokens
		 * @param useModel
		 */
		public void buildUsageTokens(UsageTokens usageTokens, String useModel) {
			MetadataUtils.buildUsageTokens(this, usageTokens, useModel);
		}

	}


	/**
	 * 定义智能体 的身份验证方案和凭据。
	 */
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AgentAuthentication implements Serializable {

		// 授权方式 如 bearer , basic
		protected List<String> schemes;

		// 身份验证凭据。如果不需要，可以是字符串（例如token）或null。
		protected String credentials;
	}


	/**
	 * 授权信息
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@EqualsAndHashCode(callSuper=true)
	public static class AuthenticationInfo  extends AgentAuthentication  {
		// 允许动态添加的配置项
		private Map<String, Object> modelConfig;
	}

	/**
	 * TODO 授权鉴权后面实现
	 */
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PushNotificationConfig  implements Serializable  {

		//推送通知的url
		private String url;

		// 推送通知请求中包含的用于验证/身份验证的令牌。
		private String token;

		// 智能体调用通知URL所需的可选身份验证详细信息。
		private AuthenticationInfo authentication;
	}

	/**
	 * 产物工件，远程智能体返回的内容载体（由Part组成）
	 */
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Artifact implements Serializable {

		/**
		 *  Artifact name
		 */
		private String name;

		/**
		 * Artifact description.
		 */
		private String description;

		/**
		 *  Content parts.
		 */
		private List<Part> parts;

		/**
		 * 上下文传递信息
		 */
		private Map<String, Object> metadata;

		/**
		 *  Order index, useful for streaming/updates.
		 *  追加情况的读取索引
		 */
		@Builder.Default
		private int index = 0;

		/**
		 * For streaming, indicates if content should append to artifact at the same index.
		 * 是否内容有追加，如果有追加则为true, 如果没有追加，表示一个part完成，则为false
		 */
		@Builder.Default
		private Boolean append = false;

		/**
		 * For streaming, indicates the final chunk for this artifact.
		 * 最后一块
		 */
		@Builder.Default
		private Boolean lastChunk= true;
	}

	/**
	 *  任务状态
	 */
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class TaskStatus  implements Serializable  {

		/**
		 * 状态内容
		 *  submitted, working, input-required.... 都是读取 record.status.message, 这样完成和未完成两个状态数据结构分离开了，并且不同状态存储不一样，这样就减少冗余存储
		 */
		private TaskState state;

		/**
		 * 消息
		 */
		private Message message;

		@Builder.Default
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
		private String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);


		public TaskStatus(TaskState state, Message message) {
			this.state = state;
			this.timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
			this.message = message;
		}

		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}
	}
}
