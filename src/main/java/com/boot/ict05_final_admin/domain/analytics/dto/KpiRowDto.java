package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * KPI 테이블 행 DTO.
 *
 * <p>상단 Total 행과 점포별 행에 공통 사용되는 단위 레코드이다.</p>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiRowDto {

    /** 점포명(또는 "Total") */
    private String storeName;

    /** 총매출 */
    private BigDecimal sales;

    /** 결제건수 */
    private Long transaction;

    /** 주문당 메뉴 수(UPT) */
    private BigDecimal upt;

    /** 주문당 평균 매출(ADS) */
    private BigDecimal ads;

    /** 메뉴별 평균 매출(AUR) */
    private BigDecimal aur;

    /** 전월 대비 성장률(%) */
    private BigDecimal compMoM;

    /** 전년 대비 성장률(%) */
    private BigDecimal compYoY;

    /** 기간 라벨(일별: yyyy-MM-dd, 월별: yyyy-MM) */
    private String date;

    /** 방문 비율(%) */
    private BigDecimal ratioVisit;

    /** 포장 비율(%) */
    private BigDecimal ratioTakeout;

    /** 배달 비율(%) */
    private BigDecimal ratioDelivery;
}
