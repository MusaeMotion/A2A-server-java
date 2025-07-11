package com.musaemotion.a2a.common.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Maps;
import com.musaemotion.a2a.common.IMetadata;
import com.musaemotion.a2a.common.base.UsageTokens;
import com.musaemotion.a2a.common.utils.MetadataUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Map;

import static com.musaemotion.a2a.common.constant.MetaDataKey.*;
import static com.musaemotion.a2a.common.constant.MetaDataKey.FREQUENCY;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/11  11:08
 * @description 任务抽象类
 */
@Data
@SuperBuilder
@NoArgsConstructor
public  class AbstractTask  implements Serializable, IMetadata  {

	/**
	 * 这个是任务id
	 */
	protected String id;

	/**
	 * map数据
	 */
	protected Map<String, Object> metadata  = Maps.newHashMap();


	/**
	 * 构建使用Token
	 * @param usageTokens
	 * @param useModel
	 */
	public void buildUsageTokens(UsageTokens usageTokens, String useModel) {
		MetadataUtils.buildUsageTokens(this, usageTokens, useModel);
	}

	/**
	 * 合并MetaData
	 * @param newMetadata
	 */
	public void mergeMetaData(Map<String, Object> newMetadata) {
		if (this.metadata == null) {
			this.metadata = Maps.newHashMap();
		}
		if(newMetadata != null) {
			this.metadata.putAll(newMetadata);
		}
	}
}
