package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 주문 분석 상단 카드 영역 DTO.
 *
 * <p>YTD 기준의 주문·매출 지표와 카테고리/메뉴 Top 리스트를 포함한다.</p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrdersCardsDto {

    /** YTD 주문건수 */
    private Long transaction;

    /** YTD 배달 매출 */
    private BigDecimal deliverySales;

    /** YTD 포장 매출 */
    private BigDecimal takeoutSales;

    /** YTD 방문 매출 */
    private BigDecimal visitSales;

    /** 카테고리(수량 기준) Top 리스트 — 수량 내림차순 정렬 */
    private List<CategoryStat> categoriesByCount;

    /** 카테고리(매출 기준) Top 리스트 — 매출 내림차순 정렬 */
    private List<CategoryStat> categoriesBySales;

    /** YTD 판매수량 합계 (Σ quantity) */
    private Long menuCount;

    /** 상위 메뉴 리스트 (기본: 매출 또는 수량 기준 정렬) */
    private List<TopMenuItem> topMenus;
}
