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

import lombok.Data;
import org.springframework.core.io.Resource;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.framework.model
 * @project：A2A
 * @date：2025/4/28 13:21
 * @description：请完善描述
 */
@Data
public class FileInfo {

    /**
     * 文件 content-type
     */
    private String mime;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件名称 ClassPathResource 或者 FileSystemResource 对象
     */
    private Resource resource;
}
