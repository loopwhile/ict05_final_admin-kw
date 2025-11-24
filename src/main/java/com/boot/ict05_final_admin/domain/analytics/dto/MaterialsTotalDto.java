package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 재료분석 Total(요약행) DTO.
 *
 * <p>서비스 계층에서 원천 합계를 바탕으로 파생지표를 계산해 채워 넣는다.</p>
 *
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialsTotalDto {

	/** 현재 가맹점 재고 총량(스냅샷) */
	private BigDecimal totalInventoryQty;

	/** YTD 발주수량 합 */
	private Long totalPurchaseOrderQty;

	/** 회전율(간이식: Used / Current) */
	private BigDecimal turnoverRate;

	/** 총 이익 = SellingSum - CostSum */
	private BigDecimal totalProfit;

	/** 마진율(%) = totalProfit / SellingSum * 100 */
	private BigDecimal marginRate;

	/** 평균 사용량 = Used / 일수 */
	private BigDecimal avgUsage;
}
