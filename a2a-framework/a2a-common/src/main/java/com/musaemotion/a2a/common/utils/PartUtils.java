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

package com.musaemotion.a2a.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musaemotion.a2a.common.constant.ArtifactDataKey;
import com.musaemotion.a2a.common.base.Common;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.common.utils
 * @project：A2A
 * @date：2025/5/12 19:39
 * @description：请完善描述
 */
@Slf4j
public class PartUtils {


	/**
	 * 从message消息里获取一条TextPart 的文本内容消息
	 * @param message
	 * @return
	 */
	public static String getFirstOneTextContentByMessage(Common.Message message) {
		var op =  message.getParts().stream().filter(item->item instanceof Common.TextPart).findFirst();
		if(op.isPresent()) {
			String text = ((Common.TextPart)op.get()).getText();
			Map<String, Object> fileMap = firstOneFilePartToMapByParts(message.getParts());
			if(fileMap.size()>0){
				ObjectMapper mapper = new ObjectMapper();
				try {
					text+=" \n 附件信息如下：\n "+ mapper.writeValueAsString(fileMap);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			}
			return text;
		}
		throw new RuntimeException("没有找到TextPart对象");
	}

	/**
	 * 从parts 列表从获取 FilePart
	 * @param parts
	 * @return
	 */
	public static List<Common.Part> getFilePartByParts(List<Common.Part> parts) {
		return parts.stream().filter(part -> !(part instanceof Common.TextPart)).collect(Collectors.toUnmodifiableList());
	}

	/**
	 * 从message消息里获取 FilePart
	 * @param message
	 * @return
	 */
	public static List<Common.Part> getFilePartByMessage(Common.Message message) {
		return getFilePartByParts(message.getParts());
	}


	/**
	 * 从Parts列表里取第一条FilePart转换成Map格式
	 * @param parts
	 * @return
	 */
	private static Map<String, Object> firstOneFilePartToMapByParts(List<Common.Part> parts) {
		var op = parts.stream().filter(item -> item instanceof Common.FilePart).findFirst();
		if (op.isPresent()) {
			Common.FileContent fileContent = ((Common.FilePart) op.get()).getFile();
			return Map.of(
					ArtifactDataKey.ARTIFACT_MIME_TYPE, fileContent.getMimeType(),
					ArtifactDataKey.ARTIFACT_FILE_NAME, fileContent.getName() == null ? "通过消息内容获取" : fileContent.getName()
			);
		}
		return Map.of();
	}
}
