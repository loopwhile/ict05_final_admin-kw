package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 재료 분석 카드(요약지표) DTO.
 *
 * <p>재고/발주/소진/회전율/이익/마진 등 재료 관점의 지표를 제공한다.</p>
 *
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaterialsCardsDto {

    /** 본사(물류창고) 현재 총 재고수량 */
    private Long currentOfficeInventoryQty;

    /** 전체(또는 선택된) 가맹점 현재 총 재고수량 */
    private Long currentTotalStoreInventoryQty;

    /** 조회기간 발주 수량 합 (Σ 발주수량) */
    private Long orderVolumeQty;

    /** 조회기간 소진 수량 합 (Used Quantity) */
    private Long usedQty;

    /** 회전율(Used / AvgInventory) */
    private BigDecimal turnoverRate;

    /** 매출액(재료 관련 판매액, 옵션) */
    private BigDecimal salesAmount;

    /** 이익 = Sales − Cost */
    private BigDecimal profitAmount;

    /** 평균 마진율(%) = Profit / Sales * 100 */
    private BigDecimal avgMargin;
}
