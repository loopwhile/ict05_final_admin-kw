package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 재료 분석 테이블 행 DTO.
 *
 * <p>레시피/발주 연동 전 최소 버전으로, 일/월 단위 라벨과 재고/발주/지표 컬럼을 포함한다.</p>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialsRowDto {

    /** 라벨(yyyy-MM-dd 또는 yyyy-MM) */
    private String orderDate;

    /** 점포명 */
    private String store;

    /** 재료명 */
    private String material;

    /** 시점 재고 스냅샷(일말/월말) */
    private Long storeInventoryQty;

    // ===== 일별 전용 노출 컬럼 =====

    /** 발주 ID */
    private Long purchaseOrderId;

    /** 발주일(yyyy-MM-dd) */
    private String purchaseOrderDate;

    /** 발주 수량 */
    private Long purchaseOrderQty;

    /** 발주 금액(원가 합계) */
    private BigDecimal purchaseOrderAmount;

    // ===== 계산/지표 =====

    /** 회전율 = Used / AvgInventory (소수, % 변환은 뷰에서) */
    private BigDecimal turnoverRate;

    /** 이익( Sales − Cost ) */
	private BigDecimal profit;

    /** 마진율 = Profit / Sales */
    private BigDecimal margin;

    /** 평균 사용량(일별=당일, 월별=월평균) */
    private BigDecimal avgUsage;

    // ===== 내부 키 =====

    /** 점포 ID */
    private Long storeId;

    /** 재료 ID */
    private Long materialId;
}
