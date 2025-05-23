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

package com.musaemotion.a2a.common.response;

import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.common.base.base.JSONRPCResponse;
import com.musaemotion.a2a.common.base.error.InvalidParamsError;
import com.musaemotion.a2a.common.base.error.UnsupportedOperationError;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.common
 * @project：A2A
 * @date：2025/4/22 10:12
 * @description：
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class SendTaskResponse extends JSONRPCResponse<Task> {

  /**
   * 构建没有找到错误
   * @param requestId
   * @return
   */
  public static SendTaskResponse buildResponse(String requestId, Task task) {
    SendTaskResponse response = new SendTaskResponse();
    response.setId(requestId);
    response.setResult(task);
    return response;
  }
  /**
   * 构建没有找到错误
   * @param requestId
   * @return
   */
  public static SendTaskResponse buildUnsupportedOperationError(String requestId) {
    SendTaskResponse response = new SendTaskResponse();
    return buildError(response, requestId, new UnsupportedOperationError());
  }

  /**
   * 构建没有找到错误
   * @param requestId
   * @return
   */
  public static SendTaskResponse buildInvalidParamsError(String requestId, InvalidParamsError invalidParamsError) {
    SendTaskResponse response = new SendTaskResponse();
    return buildError(response, requestId, invalidParamsError);
  }
}
