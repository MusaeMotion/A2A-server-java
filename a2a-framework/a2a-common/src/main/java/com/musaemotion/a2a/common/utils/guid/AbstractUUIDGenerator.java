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

package com.musaemotion.a2a.common.utils.guid;

import java.net.InetAddress;

/**
 * @author : [labid]
 * @version : [v1.0]
 * @description : [一句话描述该类的功能]
 * @createTime : [2023-4-6 11:21]
 * @updateUser : [labid]
 * @updateTime : [2023-4-6 11:21]
 * @updateRemark : [说明本次修改内容]
 */
public abstract class AbstractUUIDGenerator {

    private static final int IP;
    private static final int JVM = (int) (System.currentTimeMillis() >>> 8);
    private static short counter = (short) 0;

    static {
        int ipadd;
        try {
            ipadd = BytesHelper.toInt(InetAddress.getLocalHost().getAddress());
        } catch (Exception e) {
            ipadd = 0;
        }
        IP = ipadd;
    }

    public AbstractUUIDGenerator() {
    }

    protected static int getJVM() {
        return JVM;
    }

    protected static short getCount() {
        synchronized (AbstractUUIDGenerator.class) {
            if (counter < 0) {
                counter = 0;
            }
            return counter++;
        }
    }

    protected static int getIP() {
        return IP;
    }

    protected static short getHiTime() {
        return (short) (System.currentTimeMillis() >>> 32);
    }

    protected static int getLoTime() {
        return (int) System.currentTimeMillis();
    }
}
