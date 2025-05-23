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

import com.musaemotion.a2a.common.event.TaskArtifactUpdateEvent;
import com.musaemotion.a2a.common.event.TaskStatusUpdateEvent;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.common.request.GetTaskRequest;
import com.musaemotion.a2a.common.request.SendTaskRequest;
import com.musaemotion.a2a.common.request.SendTaskStreamingRequest;
import com.musaemotion.a2a.common.request.params.TaskQueryParams;
import com.musaemotion.a2a.common.request.params.TaskSendParams;
import com.musaemotion.a2a.common.response.GetTaskResponse;
import com.musaemotion.a2a.common.response.SendTaskStreamingResponse;
import com.musaemotion.a2a.common.base.base.JSONRPCResponse;
import com.musaemotion.a2a.common.constant.TaskState;
import com.musaemotion.a2a.common.utils.GuidUtils;
import com.musaemotion.a2a.agent.client.A2aClient;
import com.musaemotion.a2a.agent.client.server.PushNotificationServer;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.ConnectableFlux;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.musaemotion.a2a.agent.client.server.PushNotificationServer.NOTIFY_URL_TPL;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client
 * @project：A2A
 * @date：2025/4/25 10:35
 * @description：请完善描述
 */
@Slf4j
public class ConsoleClientCommand implements Runnable {

    private PushNotificationServer pushNotificationServer;

    private A2aClient a2aClient;

    private String sessionId;

    private Boolean streaming;

    private Boolean history;

    public ConsoleClientCommand(PushNotificationServer pushNotificationServer, A2aClient a2aClient, String sessionId, Boolean streaming, Boolean history) {
        this.pushNotificationServer = pushNotificationServer;
        this.a2aClient = a2aClient;
        this.sessionId = sessionId;
        this.streaming = streaming;
        this.history = history;
    }

    @Override
    public void run() {
        // 不为空表示启用了通知服务
        if (pushNotificationServer != null) {
            try {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(() -> {
                   pushNotificationServer.strat();
                });
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // 在这里可以添加业务逻辑
        log.info("人工智能随时准备为你服务");

        // 获取用户输入
        Scanner scanner = new Scanner(System.in);
        Boolean continueLoop = true;
        while (continueLoop) {
            String taskId = GuidUtils.createShortRandomGuid();
            log.warn("=========  开始一个新的任务 ======== {}", taskId);

            continueLoop = completeTask(taskId, scanner);

            // 如果启动聊天记录，主动拉取聊天记录打印
            if (history && continueLoop) {
                log.info("========= 历史记录 ========");
                GetTaskResponse getTaskResponse = this.a2aClient.getTask(
                        GetTaskRequest.newInstance(
                          TaskQueryParams.newInstance(taskId,10)
                        )
                );
                getTaskResponse.getResult().getArtifacts().stream().forEach(System.out::println);
            }
        }
        scanner.close();
    }


    /**
     * @param taskId
     * @param scanner
     * @return
     */
    private boolean completeTask(String taskId, Scanner scanner) {
        log.info("想说点什么? (输入'exit' 退出程序) "); // 提示符
        String input = scanner.nextLine();
        if ("exit".equalsIgnoreCase(input)) {
            log.info("程序已退出。");
            return false;
        }

        TaskSendParams taskSendParams = null;

        // 如果接受消息的服务不为空，表示已经开启，请求的时候带上通知地址信息
        if (pushNotificationServer != null) {
            taskSendParams = TaskSendParams.newUserTextInstance(taskId, this.sessionId, input, String.format(
                    NOTIFY_URL_TPL,
                    this.pushNotificationServer.getHost().getHostAddress(),
                    this.pushNotificationServer.getPort(),
                    PushNotificationServer.NOTIFY_PATH
            ));
        }else {
            taskSendParams =  TaskSendParams.newUserTextInstance(taskId, this.sessionId, input);
        }

        JSONRPCResponse<Task> taskResult;

        if(streaming) {
            ConnectableFlux<SendTaskStreamingResponse> responseConnectableFlux = this.a2aClient.sendTaskStreaming(SendTaskStreamingRequest.newInstance(taskSendParams));
            responseConnectableFlux.subscribe(sendTaskStreamingResponse -> {
                if(sendTaskStreamingResponse.getError()!=null){
                    log.info("stream error => {}", sendTaskStreamingResponse.getError().getMessage());
                    return;
                }
                if(sendTaskStreamingResponse.getResult() instanceof  TaskStatusUpdateEvent taskStatusUpdateEvent){
                    log.info("stream event => msgId：{}, body: {} ",sendTaskStreamingResponse.getId(),  taskStatusUpdateEvent.toString());
                }
                if(sendTaskStreamingResponse.getResult() instanceof  TaskArtifactUpdateEvent taskArtifactUpdateEvent){
                    log.info("stream event => msgId：{}, body: {} ", sendTaskStreamingResponse.getId(),  taskArtifactUpdateEvent.toString());
                    log.info("这次任务完成，智能体回答如下：" );
                    taskArtifactUpdateEvent.getArtifact().getParts().stream().forEach(part -> {
                        log.error(part.toString());
                    });
                }
            });
            responseConnectableFlux.connect();
            responseConnectableFlux.blockLast();

            // 拉起任务相关信息
            taskResult = this.a2aClient.getTask(
                    GetTaskRequest.newInstance(
                            TaskQueryParams.newInstance(taskId)
                    )
            );
        }else{
            taskResult = this.a2aClient.sendTask(SendTaskRequest.newInstance(taskSendParams));
        }
        Task task = taskResult.getResult();
        // 需要用户输入, 完成工件则没有信息，直接返回
        if (task.getStatus().getState().equals(TaskState.INPUT_REQUIRED)) {

            log.info("这次任务还未完成，智能体需要你来补充内容，回答如下：" );
            task.getStatus().getMessage().getParts().stream().forEach(part -> {
                log.error(part.toString());
            });

            // 如果是需要用户输入，保持之前的任务id, 继续运行
            this.completeTask(taskId, scanner);
        }
        // 如果上一次任务完成，则返回，形成新的任务id
        return true;
    }
}
