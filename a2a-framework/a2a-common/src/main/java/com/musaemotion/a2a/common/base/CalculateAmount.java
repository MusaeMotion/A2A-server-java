package com.musaemotion.a2a.common.base;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/11  15:34
 * @description 金额计算对象
 */
@Data
@Slf4j
public class CalculateAmount {

	/**
	 * 默认价格配置
	 */
	private List<ModelPriceSetting> modelPriceSetting = Lists.newArrayList();

	/**
	 * 构建函数
	 * @param modelPriceSetting
	 */
	public CalculateAmount(List<ModelPriceSetting> modelPriceSetting) {
		this.modelPriceSetting = modelPriceSetting;
	}

	/**
	 * 分转元
	 * @param fen cent 分
	 * @return
	 */
	private static BigDecimal fenToYuan(BigDecimal fen) {
		return fen.divide(BigDecimal.valueOf(100));
	}

	/**
	 * 计算金额
	 * @param priceType
	 * @param quantity
	 * @param modelName
	 * @return
	 */
	private BigDecimal calculateAmount(Integer priceType, Integer quantity, String modelName) {
		var optional = this.modelPriceSetting.stream().filter(modelPriceSetting -> modelPriceSetting.getModelName().equals(modelName)).findFirst();
		if (optional.isPresent()) {
			var modelPriceSetting = optional.get();
			return fenToYuan(modelPriceSetting.getPrice(priceType).multiply(BigDecimal.valueOf(quantity)));
		}
		log.warn("未找到模型的价格设置: {}", modelName);
		return BigDecimal.ZERO;
	}

	/**
	 * 计算按次计算，返回元
	 * @param quantity
	 * @param modelName
	 * @return
	 */
	public BigDecimal calculateCallAmount(Integer quantity, String modelName) {
	 return this.calculateAmount(0, quantity, modelName);
	}

	/**
	 * 计算按token计费，输入费用
	 * @param quantity
	 * @param modelName
	 * @return
	 */
	public BigDecimal calculateUsagePromptAmount(Integer quantity, String modelName) {
		return this.calculateAmount(1, quantity, modelName);
	}

	/**
	 * 计算按token计费，输出费用
	 * @param quantity
	 * @param modelName
	 * @return
	 */
	public BigDecimal calculateUsageCompletionAmount(Integer quantity, String modelName) {
		return this.calculateAmount(2, quantity, modelName);
	}
	/**
	 * 价格设置
	 */
	public static class ModelPriceSetting {
		/**
		 * 输入单价
		 */
		@Getter
		private BigDecimal promptUnitPrice;


		/**
		 * 输出单价
		 */
		@Getter
		private BigDecimal completionUnitPrice;

		/**
		 * 按次调用单价
		 */
		@Getter
		private BigDecimal unitPrice;


		/**
		 * 计算模式
		 */
		@Getter
		private CalculateMode calculateMode;

		/**
		 * 模型名称
		 */
		@Getter
		private String modelName;

		private ModelPriceSetting() {}


		/**
		 * 获取价格
		 * @param type
		 * @return
		 */
		public BigDecimal getPrice(Integer type) {
			if (type == 0) {
				return this.unitPrice;
			}
			if (type == 1) {
				return this.promptUnitPrice;
			}
			if (type == 2) {
				return this.completionUnitPrice;
			}
			return BigDecimal.ZERO;
		}

		/**
		 * 元转分
		 * @param yuan
		 * @return
		 */
		private static BigDecimal yuanToFen(BigDecimal yuan) {
			return yuan.multiply(BigDecimal.valueOf(100));
		}

		/**
		 * 100万token的价格
		 * @param promptOneMillionPrice
		 * @param completionOneMillionPrice
		 * @param modelName
		 * @return
		 */
		public static ModelPriceSetting createUsageOneMillion(BigDecimal promptOneMillionPrice, BigDecimal completionOneMillionPrice, String modelName) {
			ModelPriceSetting modelPriceSetting = new ModelPriceSetting();
			modelPriceSetting.calculateMode = CalculateMode.USAGE;
			modelPriceSetting.promptUnitPrice = yuanToFen(promptOneMillionPrice).divide(BigDecimal.valueOf(1_000_000));
			modelPriceSetting.completionUnitPrice = yuanToFen(completionOneMillionPrice).divide(BigDecimal.valueOf(1_000_000));
			modelPriceSetting.modelName = modelName;
			return modelPriceSetting;
		}


		/**
		 * 按照单次计算的方式
		 * @param yuan
		 * @return
		 */
		public static ModelPriceSetting createCall(BigDecimal yuan, String modelName) {
			BigDecimal fen = yuanToFen(yuan);
			ModelPriceSetting modelPriceSetting = new ModelPriceSetting();
			modelPriceSetting.calculateMode = CalculateMode.CALL;
			modelPriceSetting.unitPrice = fen;
			modelPriceSetting.modelName = modelName;
			return modelPriceSetting;
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


}
