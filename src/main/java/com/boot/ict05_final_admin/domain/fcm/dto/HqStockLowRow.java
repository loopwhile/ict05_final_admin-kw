package com.boot.ict05_final_admin.domain.fcm.dto;

import java.math.BigDecimal;

/**
 * 재고 부족 항목의 읽기 전용 행 DTO.
 *
 * <p>optimal(권장 수량)과 현재 수량을 비교하여 부족분(deficit)을 계산하는 헬퍼 메서드를 제공한다.</p>
 *
 * @param materialId   재료 ID
 * @param materialName 재료명
 * @param quantity     현재 수량
 * @param optimal      권장(목표) 수량
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public record HqStockLowRow(
        Long materialId,
        String materialName,
        BigDecimal quantity,
        BigDecimal optimal
) {
    /**
     * 권장 수량 대비 부족분을 반환한다. 값이 null인 경우 0을 반환한다.
     *
     * @return 부족분 (optimal - quantity) 또는 0
     */
    public BigDecimal deficit() {
        if (quantity == null || optimal == null) return BigDecimal.ZERO;
        return optimal.subtract(quantity);
    }
}
