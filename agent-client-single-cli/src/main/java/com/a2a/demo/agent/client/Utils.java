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

package com.a2a.demo.agent.client;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client
 * @project：A2A
 * @date：2025/4/25 17:12
 * @description：请完善描述
 */
public class Utils {

    /**
     * 构建cli参数
     * @param args
     * @return
     * @throws ParseException
     */
    public static ArgsModel buildArgs(String... args) throws ParseException {
        // 定义命令行选项
        Options options = new Options();
        options.addOption("a", "agent", true, "需要访问的agent地址");
        options.addOption("s", "session", true, "会话Id");
        options.addOption("h", "history", true, "是否开启聊天记录");
        options.addOption("en", "enableNotification", true, "是否开启通知");
        // 开启消息通知服务的地址，该地址是智能体回调发送消息的地址
        options.addOption("ne", "notificationEndpoint", true, "接受智能体通知的地址");
        // 解析命令行参数
        CommandLineParser parser = new DefaultParser();
        var cmd = parser.parse(options, args);

        return ArgsModel.builder()
                .agentUrl(cmd.getOptionValue("agent", "http://localhost:9999"))
                .session(cmd.getOptionValue("session", "0" ))
                .history(Boolean.valueOf(cmd.getOptionValue("history", "true")))
                .enableNotification(Boolean.valueOf(cmd.getOptionValue("enableNotification", "true" )))
                .notificationEndpoint(cmd.getOptionValue("notificationEndpoint", "http://localhost:5000" ))
                .build();
    }
}
