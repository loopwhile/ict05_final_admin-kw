package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * 주문 분석 테이블 행 DTO.
 *
 * <p>일/월 라벨과 점포·카테고리·메뉴 단위의 집계값을 보유한다.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersRowDto {

    /** 기간 라벨(일별: yyyy-MM-dd, 월별: yyyy-MM) */
    private String date;

    /** 점포명 */
    private String storeName;

    /** 메뉴 카테고리명 */
    private String category;

    /** 메뉴명 */
    private String menu;

    /** 메뉴 라인 매출 합계 (Σ line_total) */
    private BigDecimal menuSales;

    /** 메뉴 판매수량 합계 (Σ quantity) */
    private Long menuCount;

    /** 주문건수 (COUNT DISTINCT order) */
    private Long orderCount;

    /** 주문 매출 합계 (Σ order_total_price) */
    private BigDecimal orderSales;

    /** 주문 형태: VISIT / TAKEOUT / DELIVERY */
    private String orderType;

    /** 주문 기준일(일별: yyyy-MM-dd, 월별 대표일: yyyy-MM-01) */
    private String orderDate;

    /** 대표 주문 ID (그룹 내 MIN) */
    private Long orderId;

    /** 점포 ID (내부 매핑/버킷 병합용, 화면 미노출) */
    private Long storeId;
}
