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

import com.musaemotion.a2a.common.base.Common;
import com.musaemotion.a2a.common.base.Task;
import com.musaemotion.a2a.common.constant.MetaDataKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.host.model.response
 * @project：A2A
 * @date：2025/5/15 16:29
 * @description：请完善描述
 */
@Data
@EqualsAndHashCode(callSuper =  true)
public class CommonMessageExt extends Common.Message {

	/**
	 * 相关任务
	 */
    private List<Task> task;

	/**
	 * 当前消息花费
	 */
	private Double amount;

	/**
     *
     * @param message
     * @param task
     * @return
     */
    public static CommonMessageExt fromMessage(Common.Message message, List<Task> task) {
        CommonMessageExt commonMessageExt = new CommonMessageExt();
        BeanUtils.copyProperties(message, commonMessageExt);
        commonMessageExt.setTask(task);
        return commonMessageExt;
    }

	/**
	 *
	 * @param message
	 * @return
	 */
    public static CommonMessageExt fromMessage(Common.Message message) {
        CommonMessageExt commonMessageExt = new CommonMessageExt();
        BeanUtils.copyProperties(message, commonMessageExt);
        return commonMessageExt;
    }

	/**
	 * 获取所有消耗的tokens
	 * @return
	 */
	private Integer getTotalTokens(){
		if(this.getMetadata().containsKey(MetaDataKey.TOTAL_TOKENS)){
			return (Integer)this.getMetadata().get(MetaDataKey.TOTAL_TOKENS);
		}
		return 0;
	}

	/**
	 * 获取消耗次数
	 * @return
	 */
	private Integer getFrequency(){
		if(this.getMetadata().containsKey(MetaDataKey.FREQUENCY)){
			return (Integer)this.getMetadata().get(MetaDataKey.FREQUENCY);
		}
		return 0;
	}


	/**
	 * 计算金额
	 */
	public void calculateAmount(){


	}
}
