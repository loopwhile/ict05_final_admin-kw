package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 카테고리별 집계 통계 DTO.
 *
 * <p>카테고리 단위로 판매수량과 매출합계를 표현한다.</p>
 *
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryStat {

	/** 카테고리 ID */
	private Long categoryId;

	/** 카테고리 명 */
	private String categoryName;

	/** 판매수량 합계 (SUM(quantity)) */
	private Long units;

	/** 매출 합계 (SUM(line_total)) */
	private BigDecimal sales;
}
