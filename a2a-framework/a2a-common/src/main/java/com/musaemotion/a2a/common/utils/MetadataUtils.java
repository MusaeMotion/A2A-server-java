package com.musaemotion.a2a.common.utils;

import com.google.common.collect.Maps;
import com.musaemotion.a2a.common.IMetadata;
import com.musaemotion.a2a.common.base.UsageTokens;

import static com.musaemotion.a2a.common.constant.MetaDataKey.*;
import static com.musaemotion.a2a.common.constant.MetaDataKey.FREQUENCY;
import static com.musaemotion.a2a.common.constant.MetaDataKey.USE_MODEL;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/11  14:27
 * @description 元数据工具
 */
public class MetadataUtils {

	/**
	 * 包装mateData
	 * @param metadata
	 * @param usageTokens
	 * @param useModel
	 */
	public static void buildUsageTokens(IMetadata metadata, UsageTokens usageTokens, String useModel) {
		if (metadata.getMetadata() == null) {
			metadata.setMetadata(
					Maps.newHashMap()
			);
		}
		metadata.getMetadata().put(COMPLETION_TOKENS, usageTokens.getCompletionTokens());
		metadata.getMetadata().put(PROMPT_TOKENS, usageTokens.getPromptTokens());
		metadata.getMetadata().put(TOTAL_TOKENS, usageTokens.getTotalTokens());
		metadata.getMetadata().put(FREQUENCY, usageTokens.getFrequency());
		metadata.getMetadata().put(USE_MODEL, useModel);
	}
}
