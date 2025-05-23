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

package com.a2a.demo.agent.server;

import com.a2a.demo.agent.server.page.ItemDto;
import com.a2a.demo.agent.server.page.SearchPage;
import com.google.common.collect.Lists;
import com.microsoft.playwright.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.server
 * @project：A2A
 * @date：2025/5/16 11:48
 * @description：请完善描述
 */
@Slf4j
public class WebTools {

	@Getter
	private String program;


	@Tool(description = "在百度搜索新闻", returnDirect = true)
	public List<ItemDto> searchNews(
			@ToolParam(required = true, description = "需要检索的内容") String searchContent,
			@ToolParam(required = true, description = "栏目名称：资讯、图片") String program
	) {
		try (Playwright playwright = Playwright.create()) {
			Browser browser = playwright.chromium().launch(
					new BrowserType.LaunchOptions()
							.setSlowMo(500)
			);
			this.program = program;
			// 创建上下文
			BrowserContext context = browser.newContext();
			Page page = context.newPage();
			SearchPage searchPage = new SearchPage(SearchPage.SearchEngines.BAIDU, page);
			return searchPage.search(searchContent, program);
		}
	}

}
