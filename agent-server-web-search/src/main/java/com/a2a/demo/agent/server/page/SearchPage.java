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

package com.a2a.demo.agent.server.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.server
 * @project：A2A
 * @date：2025/5/16 14:51
 * @description：请完善描述
 */
@Slf4j
public class SearchPage {

	private SearchEngines searchEngine;

	private final Page page;

	private final Locator searchInput;

	/**
	 * 搜索引擎
	 * @param searchEngine
	 * @param page
	 */
	public SearchPage(SearchEngines searchEngine, Page page) {
        this.searchEngine = searchEngine;
		this.page = page;
		this.searchInput = page.locator(searchEngine.getSearchInputSelector());
	}

	/**
	 * 跳转到搜索引擎
	 */
	private void navigate() {
		this.page.navigate(this.searchEngine.getUrl(), new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

	}

	/**
	 * 搜索
	 * @param text
	 */
	public List<ItemDto>  search(String text, String textProgram) {
		this.navigate();
		this.searchInput.click();
		this.searchInput.fill(text);
		this.searchInput.press("Enter");
		assertThat(page).hasTitle(Pattern.compile(text + "*"));
		return this.toProgram(textProgram);

	}

	/**
	 * 到栏目
	 * @param textProgram
	 */
	private List<ItemDto> toProgram(String textProgram) {
		Locator elements = this.page.locator(
				this.searchEngine.getProgramSelector()
		);
		List<ProgramDto> programs = elements.all().stream().map(element -> {
			return ProgramDto.builder()
					.programName(element.textContent())
					.locator(element)
					.url(element.getAttribute("href"))
					.build();

		}).collect(Collectors.toList());
		var op = programs.stream().filter(item->item.getProgramName().contains(textProgram)).findFirst();
		if (op.isPresent()) {
			op.get().getLocator().click();
			if("资讯".equals(textProgram)) {
				return listNews();
			}
            return listImage();
		}
		return List.of();
	}

	/**
	 *
	 */
	private List<ItemDto> listImage() {
		// 定位到具有特定 class 的所有元素
		var elements = page.locator("img[src]");
		List<ItemDto> items = new ArrayList<>();
		elements.all().forEach(element -> {
			var item = new ItemDto();
			item.setUrl(element.getAttribute("src"));
			item.setContent(element.getAttribute("src"));
			items.add(
					item
			);
		});
		return items;
	}
	/**
	 *
	 */
	private List<ItemDto> listNews() {
		// 定位到具有特定 class 的所有元素
		var elements = page.locator(this.searchEngine.getNewListSelector());
		List<ItemDto> items = new ArrayList<>();
		elements.all().stream().forEach(element -> {
			var item = new ItemDto();
			item.setContent(
					element.textContent().replaceAll("\\s+", "")
			);
			var links = element.locator("a");
			// 获取所有链接的文本内容和链接地址
			links.all().forEach(link -> {
				if (link.isVisible()) {
					String href = link.getAttribute("href");
					item.setUrl(href);
				}
			});
			items.add(item);
		});
		return items;
	}


	/**
	 * 搜索引擎枚举
	 */
	public enum SearchEngines {
		BAIDU("baidu","https://www.baidu.com","input[name=\"wd\"]", "#s_tab_inner > a", ".c-container"),
		BING("bing","https://www.baidu.com", "input[id=\"sb_form_q\"]", "#s_tab_inner > a", ".c-container");
		@Getter
		private String name;
		@Getter
		private String url;
		@Getter
		private String searchInputSelector;
		@Getter
		private String programSelector;
		@Getter
		private String newListSelector;

		SearchEngines(String name, String url, String searchInputSelector, String programSelector, String newListSelector) {
			this.url = url;
			this.name = name;
			this.searchInputSelector = searchInputSelector;
			this.programSelector = programSelector;
			this.newListSelector = newListSelector;
		}
	}
}
