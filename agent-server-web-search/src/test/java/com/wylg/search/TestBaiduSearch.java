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

package com.musaemotion.search;

import com.a2a.demo.agent.server.page.ItemDto;
import com.a2a.demo.agent.server.page.SearchPage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.search
 * @project：A2A
 * @date：2025/5/16 15:54
 * @description：请完善描述
 */
@Slf4j
public class TestBaiduSearch extends TestSearch {

	@Test
	void baiduSearch() {
		SearchPage searchPage = new SearchPage(SearchPage.SearchEngines.BAIDU, this.page);
		List<ItemDto> items = searchPage.search("张学友","资讯");
        log.info(items.toString());
	}
}
