package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * 주문분석 Total(요약행) DTO.
 *
 * <p>테이블 하단 Total 행에 표시되는 전체 합계를 담는다.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersTotalDto {

	/** 메뉴 수량 합계 (Σ cod.quantity) */
	private Long totalMenuCount;

	/** 메뉴 매출 합계 (Σ cod.lineTotal) */
	private BigDecimal totalMenuSales;

	/** 주문건수 (COUNT DISTINCT co.id) */
	private Long totalOrderCount;

	/** 주문 매출 합계 (Σ co.totalPrice) */
	private BigDecimal totalOrderSales;
}
