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

package com.a2a.demo.agent.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.controller
 * @project：a2a-github
 * @date：2025/5/22 11:39
 * @description：请完善描述
 */
@Controller
@Slf4j
public class FrontApiController {

	/**
	 * 集成前端
	 * @return
	 */
	@GetMapping("/{path:[^\\.]*}")
	public String forward() {
		return "forward:/index.html";
	}
}
