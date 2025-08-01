package com.musaemotion.a2a.common.base;

import com.google.common.collect.Maps;
import com.musaemotion.a2a.common.IMetadata;
import com.musaemotion.a2a.common.constant.MetaDataKey;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/31  16:37
 * @description 计算委托实现
 */
public class CalculateChargeableDelegate implements CalculateChargeable {

	private IMetadata metadata = null;

	public CalculateChargeableDelegate(IMetadata metadata) {
		this.metadata = metadata;
	}

	private  Map<String, Object> getMetadata() {
		return this.metadata.getMetadata();
	}

	/**
	 * @return
	 */
	@Override
	public Integer getFrequency() {
		if (this.getMetadata().containsKey(MetaDataKey.FREQUENCY)) {
			return (Integer) this.getMetadata().get(MetaDataKey.FREQUENCY);
		}
		return 0;
	}

	/**
	 * @return
	 */
	@Override
	public String getModelName() {
		if (this.getMetadata().containsKey(MetaDataKey.USE_MODEL)) {
			return (String) this.getMetadata().get(MetaDataKey.USE_MODEL);
		}
		return "";
	}

	/**
	 * @return
	 */
	@Override
	public Integer getPromptTokens() {
		if (this.getMetadata().containsKey(MetaDataKey.PROMPT_TOKENS)) {
			return (Integer) this.getMetadata().get(MetaDataKey.PROMPT_TOKENS);
		}
		return 0;
	}

	/**
	 * @return
	 */
	@Override
	public Integer getCompletionTokens() {
		if (this.getMetadata().containsKey(MetaDataKey.COMPLETION_TOKENS)) {
			return (Integer) this.getMetadata().get(MetaDataKey.COMPLETION_TOKENS);
		}
		return 0;
	}

	/**
	 * @param amount
	 */
	@Override
	public void setTotalAmount(BigDecimal amount) {
		this.getMetadata().put(
				MetaDataKey.TOTAL_AMOUNT,
				amount
		);
	}

	/**
	 * @return
	 */
	@Override
	public BigDecimal getPromptTokensAmount() {
		if (this.getMetadata().containsKey(MetaDataKey.PROMPT_TOKENS_AMOUNT)) {
			return (BigDecimal) this.getMetadata().get(MetaDataKey.PROMPT_TOKENS_AMOUNT);
		}
		return BigDecimal.ZERO;
	}

	/**
	 * @param amount
	 */
	@Override
	public void setPromptTokensAmount(BigDecimal amount) {
		this.getMetadata().put(
				MetaDataKey.PROMPT_TOKENS_AMOUNT,
				amount
		);
	}

	/**
	 * @return
	 */
	@Override
	public BigDecimal getCompletionTokensAmount() {
		if (this.getMetadata().containsKey(MetaDataKey.COMPLETION_TOKENS_AMOUNT)) {
			return (BigDecimal) this.getMetadata().get(MetaDataKey.COMPLETION_TOKENS_AMOUNT);
		}
		return BigDecimal.ZERO;
	}

	/**
	 * @param amount
	 */
	@Override
	public void setCompletionTokensAmount(BigDecimal amount) {
		this.getMetadata().put(
				MetaDataKey.COMPLETION_TOKENS_AMOUNT,
				amount
		);
	}

	/**
	 * 计算
	 * @param calculateAmount
	 */
	public void calAmount(CalculateAmount calculateAmount){
		if (this.getFrequency() > 0) {
			this.setTotalAmount(calculateAmount.calculateCallAmount(this.getFrequency(), this.getModelName()));
			return;
		}
		this.setCompletionTokensAmount(calculateAmount.calculateUsageCompletionAmount(this.getCompletionTokens(), this.getModelName()));
		this.setPromptTokensAmount(calculateAmount.calculateUsagePromptAmount(this.getPromptTokens(), this.getModelName()));
		this.setTotalAmount(this.getCompletionTokensAmount().add(this.getPromptTokensAmount()));
	}
}
