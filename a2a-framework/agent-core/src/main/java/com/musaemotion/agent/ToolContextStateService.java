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

package com.musaemotion.agent;

import java.util.Map;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.agent
 * @project：A2A
 * @date：2025/4/28 17:37
 * @description：调用前回调
 */
public interface ToolContextStateService {

   void initStateForToolContext(Map<String, Object> state);
}
