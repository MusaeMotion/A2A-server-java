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

package com.musaemotion.a2a.agent.host.ext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.ext
 * @project：A2A
 * @date：2025/4/28 18:06
 * @description：请完善描述
 */

@Slf4j
public class MyToolExecutionExceptionProcessor implements ToolExecutionExceptionProcessor {
    @Override
    public String process(ToolExecutionException exception) {
        log.error("工具调用出现异常: {}", exception.getToolDefinition().name());
        exception.printStackTrace();
        return exception.getMessage();
    }

    public static MyToolExecutionExceptionProcessor.Builder builder() {
        return new MyToolExecutionExceptionProcessor.Builder();
    }

    public static class Builder {

        public MyToolExecutionExceptionProcessor build() {
            return new MyToolExecutionExceptionProcessor();
        }

    }
}

