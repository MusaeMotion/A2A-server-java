package com.musaemotion.a2a.agent.host.model;

import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/11  15:34
 * @description 金额计算对象
 */
public class CalculateAmountSetting {

	/**
	 * 单价
	 */
	private BigDecimal unitPrice;
	/**
	 * 10万价格
	 */
	@Setter
	private BigDecimal millionOnePrice;


	private CalculateAmountSetting() {}

	/**
	 *
	 * @param millionOnePrice
	 * @return
	 */
	public static CalculateAmountSetting createUsageMillionOne(BigDecimal millionOnePrice) {
		CalculateAmountSetting calculateAmountSetting = new CalculateAmountSetting();
		calculateAmountSetting.setMillionOnePrice(millionOnePrice);
		return calculateAmountSetting;
	}


	/**
	 * 按照单次计算的方式
	 * @param millionOnePrice
	 * @return
	 */
	public static CalculateAmountSetting createCall(BigDecimal millionOnePrice) {
		CalculateAmountSetting calculateAmountSetting = new CalculateAmountSetting();
		calculateAmountSetting.setMillionOnePrice(millionOnePrice);
		return calculateAmountSetting;
	}

	/**
	 * 计算方式
	 */
	public enum CalculateMode {
		// 使用量
		USAGE,
		// 次数
		CALL;
	}
}
