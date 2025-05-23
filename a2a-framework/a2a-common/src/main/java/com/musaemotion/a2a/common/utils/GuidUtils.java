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

package com.musaemotion.a2a.common.utils;


import com.musaemotion.a2a.common.utils.guid.AbstractUUIDGenerator;

import java.util.UUID;

/**
 * @author : [labid]
 * @version : [v1.0]
 * @description : [一句话描述该类的功能]
 * @createTime : [2023-4-6 11:20]
 * @updateUser : [labid]
 * @updateTime : [2023-4-6 11:20]
 * @updateRemark : [说明本次修改内容]
 */
public class GuidUtils extends AbstractUUIDGenerator {
    private static final String sep = "-";

    /**
     * 生成有序Guid，用于数据表主键字段
     *
     * @return
     */
    public static String createGuid() {
        return
                format(getJVM()) + sep
                        + format(getHiTime()) + sep
                        + format(getLoTime()) + sep
                        + format(getIP()) + sep
                        + format(getCount());
    }

    /**
     * 创建无序Guid,用于普通字段
     * @return
     */
    public static String createRandomGuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 不带横杠的版本
     * @return
     */
    public static String createShortRandomGuid() {
        return createRandomGuid().replace("-", "");
    }

    /**
     * 长模式
     * @param intValue
     * @return
     */
    protected static String format(int intValue) {
        String formatted = Integer.toHexString(intValue);
        StringBuilder buf = new StringBuilder("00000000");
        buf.replace(8 - formatted.length(), 8, formatted);
        return buf.toString();
    }

    /**
     * 短模式
     * @param shortValue
     * @return
     */
    protected static String format(short shortValue) {
        String formatted = Integer.toHexString(shortValue);
        StringBuilder buf = new StringBuilder("0000");
        buf.replace(4 - formatted.length(), 4, formatted);
        return buf.toString();
    }
}
