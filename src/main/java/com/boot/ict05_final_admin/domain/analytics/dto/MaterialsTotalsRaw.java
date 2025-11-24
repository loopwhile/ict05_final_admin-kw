package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 재료분석 Total 계산을 위한 원천 합계 DTO.
 *
 * <p>Repository에서 집계된 원천 수치를 담고, 서비스에서 Turnover/Margin/AvgUsage 등 파생지표를 계산한다.</p>
 *
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialsTotalsRaw {

	/** 현재 기준 가맹점 재고 총량 (store_inventory.inventory_quantity) */
	private BigDecimal currentStoreInventoryQty;

	/** YTD 발주수량 합 (Σ rod.detailCount) */
	private Long orderVolumeQty;

	/** YTD 재료 소진량 합 (Σ cod.quantity × mr.recipeQty) */
	private BigDecimal usedQty;

	/** YTD 재료 원가 합 (Σ rod.detailUnitPrice × rod.detailCount) */
	private BigDecimal costSum;

	/** YTD 재료 판매가 기준액 합 (Σ sm.sellingPrice × rod.detailCount) */
	private BigDecimal sellingSum;
}
