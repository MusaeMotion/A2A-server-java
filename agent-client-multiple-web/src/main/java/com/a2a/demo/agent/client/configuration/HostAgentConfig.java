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

package com.a2a.demo.agent.client.configuration;

import com.google.common.collect.Lists;
import com.musaemotion.a2a.common.base.CalculateAmount;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.configuration
 * @project：A2A
 * @date：2025/4/29 17:04
 * @description：hostAgent 配置
 */
@Configuration
public class HostAgentConfig {

	/**
	 * 模型价格配置对象
	 */
	public static CalculateAmount calculateAmount = new CalculateAmount(Lists.newArrayList(
			CalculateAmount.ModelPriceSetting.createUsageOneMillion(BigDecimal.valueOf(8) ,BigDecimal.valueOf(8),"qwen-plus"),
			CalculateAmount.ModelPriceSetting.createCall(BigDecimal.valueOf(1) ,"zhipu"),
			CalculateAmount.ModelPriceSetting.createCall(BigDecimal.valueOf(2) ,"qwen-vl-max-latest")
	));
}


