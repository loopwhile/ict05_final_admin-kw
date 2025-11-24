package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryAdjustment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 재고 조정 커스텀 리포지토리.
 *
 * <p>인덱스 친 createdAt 기준 기간 필터와 합계, 최신 시각 조회를 제공한다.</p>
 */
public interface InventoryAdjustmentRepositoryCustom {

    /**
     * 재고ID와 기간으로 조정 이력을 조회한다(내림차순).
     *
     * @param inventoryId 대상 재고 PK
     * @param from        시작 시각(옵션, null이면 하한 없음)
     * @param to          종료 시각(옵션, null이면 상한 없음)
     */
    List<InventoryAdjustment> findByInventoryAndPeriod(Long inventoryId, LocalDateTime from, LocalDateTime to);

    /**
     * 재고ID와 기간으로 증감치 합계를 조회한다.
     *
     * @param inventoryId 대상 재고 PK
     * @param from        시작 시각(옵션)
     * @param to          종료 시각(옵션)
     * @return difference 합계(없으면 0)
     */
    BigDecimal sumQuantityByInventoryAndPeriod(Long inventoryId, LocalDateTime from, LocalDateTime to);

    /**
     * 재고ID 기준 마지막 조정(createdAt 최대값) 시각을 조회한다.
     *
     * @param inventoryId 대상 재고 PK
     * @return 마지막 시각(없으면 null)
     */
    LocalDateTime lastAdjustmentAtByInventory(Long inventoryId);
}
