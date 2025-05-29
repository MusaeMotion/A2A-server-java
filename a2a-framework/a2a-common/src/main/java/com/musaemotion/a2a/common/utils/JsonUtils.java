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

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.server.utils
 * @project：A2A
 * @date：2025/4/27 11:19
 * @description：请完善描述
 */
public class JsonUtils {


	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 转换成字符串
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		}catch (JsonProcessingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}


	/**
	 * 工具类
	 * @param str
	 * @return
	 */
	public static boolean isJsonString(String str) {
		try {
			objectMapper.readTree(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
