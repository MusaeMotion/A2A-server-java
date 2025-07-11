package com.musaemotion.a2a.common.base;

import lombok.Data;
/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/10  16:29
 * @description token耗用对象
 */
@Data
public class UsageTokens {
	/**
	 * 提示词 token
	 */
    private int promptTokens = 0;

	/**
	 * 生成 token
	 */
	private int completionTokens = 0;

	/**
	 * 总token数量
	 */
	private int totalTokens = 0;

	/**
	 * 使用次数
	 */
	private int frequency = 0;

	/**
	 *
	 * @param completionTokens
	 * @param promptTokens
	 * @param totalTokens
	 * @return
	 */
	public static UsageTokens fromUsage(Integer completionTokens, Integer promptTokens, Integer totalTokens) {
		UsageTokens usageTokens = new UsageTokens();
		usageTokens.setCompletionTokens(completionTokens);
		usageTokens.setTotalTokens(totalTokens);
		usageTokens.setPromptTokens(promptTokens);
		return usageTokens;
	}

	/**
	 * 使用次数
	 * @param frequency
	 * @return
	 */
	public static UsageTokens fromUsage(int frequency){
		UsageTokens usageTokens = new UsageTokens();
		usageTokens.setTotalTokens(frequency);
		return usageTokens;
	}

}
