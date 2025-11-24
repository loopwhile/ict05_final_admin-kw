package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * KPI 상단 카드 영역 DTO.
 *
 * <p>YTD 지표와 구성비(주문형태 비율)를 포함한다.</p>
 *
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KpiCardsDto {

    /** YTD 총매출 */
    private BigDecimal sales;

    /** YTD 결제건수 */
    private Long transaction;

    /** UPT(총판매메뉴수 / 결제건수) */
    private BigDecimal upt;

    /** ADS(총매출 / 결제건수) */
    private BigDecimal ads;

    /** AUR(총매출 / 총판매메뉴수) */
    private BigDecimal aur;

    /** M/M 비교: (MTD / PMTD) - 1 */
    private BigDecimal compMoM;

    /** Y/Y 비교: (YTD / 전년동기간 YTD) - 1 */
    private BigDecimal compYoY;

    /** 방문 비율(%) 0~100 */
    private BigDecimal visitRatio;

    /** 포장 비율(%) 0~100 */
    private BigDecimal takeoutRatio;

    /** 배달 비율(%) 0~100 */
    private BigDecimal deliveryRatio;
}
