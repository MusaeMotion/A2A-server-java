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

package com.musaemotion.a2a.common.base;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.common.model
 * @project：A2A
 * @date：2025/4/28 16:08
 * @description：请完善描述
 */
public class FilePart {
    private FileBlob inlineData;

    public FilePart(FileBlob inlineData) {
        this.inlineData = inlineData;
    }

    public FileBlob getInlineData() {
        return inlineData;
    }
}
