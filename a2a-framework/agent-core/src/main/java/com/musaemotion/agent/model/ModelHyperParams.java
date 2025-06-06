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

package com.musaemotion.agent.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModelHyperParams {

  @Builder.Default
  private Double frequencyPenalty = 0.1;

  @Builder.Default
  private Double presencePenalty = 0.1;

  @Builder.Default
  private Double temperature = 0.75;

  @Builder.Default
  private Double topP = 0.95;

  /**
   * 最大token
    */
  @Builder.Default
  private int maxTokens = 2400;

}
