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

package com.musaemotion.a2a.common.base.error;

import com.musaemotion.a2a.common.base.base.JSONRPCError;
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
public class UnsupportedOperationError  extends JSONRPCError {
  public UnsupportedOperationError() {
    this.setCode(-32004);
    this.setMessage("This operation is not supported");
  }
}
