package com.musaemotion.a2a.common.base;

import java.math.BigDecimal;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/31  16:28
 * @description  计费维度接口：统一暴露计费所需的所有字段，任何对象实现了该接口即可被计费服务直接消费
 */

public interface CalculateChargeable {

	/** 调用次数（>0 表示按次计费，<=0 表示按 token 计费） */
	Integer getFrequency();

	/** 模型名称 */
	String getModelName();

	/** 消耗的 prompt token 数量 */
	Integer getPromptTokens();

	/** 消耗的 completion token 数量 */
	Integer getCompletionTokens();

	/* ======== 以下为金额字段的 getter/setter ======== */

	/** 总费用（调用费用或 token 费用之和） */
	void setTotalAmount(BigDecimal amount);

	/** prompt token 对应的费用 */
	BigDecimal getPromptTokensAmount();
	void setPromptTokensAmount(BigDecimal amount);

	/** completion token 对应的费用 */
	BigDecimal getCompletionTokensAmount();
	void setCompletionTokensAmount(BigDecimal amount);

	/**
	 * 实际计算
	 * @param calculateAmount
	 */
	void calAmount(CalculateAmount calculateAmount);
}
