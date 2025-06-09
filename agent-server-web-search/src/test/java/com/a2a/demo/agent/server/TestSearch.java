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

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.search
 * @project：A2A
 * @date：2025/5/16 15:53
 * @description：请完善描述
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestSearch {

	static Playwright playwright;
	static Browser browser;

	@BeforeAll
	void launchBrowser() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch(
				new BrowserType.LaunchOptions()
						.setSlowMo(500)
		);
	}

	@AfterAll
	void closeBrowser() {
		playwright.close();
	}


	BrowserContext context;
	Page page;

	@BeforeEach
	void createContextAndPage() {
		context = browser.newContext();
		page = context.newPage();
	}

	@AfterEach
	void closeContext() {
		context.close();
	}
}
