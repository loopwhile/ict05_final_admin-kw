package com.boot.ict05_final_admin.domain.inventory.entity;

import java.math.BigDecimal;

/**
 * 재고 상태 Enum.
 *
 * <p>재고 상태를 정의하며, 각 항목은 한글 설명(description)을 가진다.</p>
 *
 * <p>재고 상태:</p>
 * <ul>
 *   <li>SUFFICIENT: 충분</li>
 *   <li>LOW: 부족</li>
 *   <li>SHORTAGE: 품절</li>
 * </ul>
 */
public enum InventoryStatus {

    /** 충분: 적정 재고 이상 */
    SUFFICIENT("충분"),

    /** 부족: 적정 재고 미만 */
    LOW("부족"),

    /** 품절: 재고 0 이하 */
    SHORTAGE("품절");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자.
     *
     * @param description 상태의 한글 설명
     */
    InventoryStatus(String description) {
        this.description = description;
    }

    /**
     * 상태의 한글 설명을 반환한다.
     *
     * @return 한글 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 상태 계산(null-safe).
     *
     * <ul>
     *   <li>quantity ≤ 0 → SHORTAGE</li>
     *   <li>optimalQuantity == null → quantity &gt; 0이면 SUFFICIENT</li>
     *   <li>quantity &lt; optimalQuantity → LOW</li>
     *   <li>그 외 → SUFFICIENT</li>
     * </ul>
     *
     * @param quantity 현재 수량
     * @param optimalQuantity 적정 수량(선택)
     * @return 계산된 재고 상태
     */
    public static InventoryStatus from(BigDecimal quantity, BigDecimal optimalQuantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) return SHORTAGE;
        if (optimalQuantity == null) return SUFFICIENT;
        return quantity.compareTo(optimalQuantity) < 0 ? LOW : SUFFICIENT;
    }

    /** 하위 호환용 별칭 */
    public static InventoryStatus calculate(BigDecimal quantity, BigDecimal optimalQuantity) {
        return from(quantity, optimalQuantity);
    }
}
