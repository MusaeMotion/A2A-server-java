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

package com.musaemotion.a2a.agent.host.model.response;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.a2a.demo.agent.client.model
 * @project：A2A
 * @date：2025/4/30 17:34
 * @description：请完善描述
 */
@Data
public class PageInfo<T>  implements Serializable {

    /**
     * 列表数据
     */
    private List<T> list;

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Integer total;

    /**
     * 总页数
     */
    private Integer totalPages;

}
