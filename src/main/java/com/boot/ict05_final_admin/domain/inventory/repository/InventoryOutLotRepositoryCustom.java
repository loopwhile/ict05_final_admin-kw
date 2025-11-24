package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;

import java.util.List;

/**
 * 출고-LOT 커스텀 리포지토리.
 */
public interface InventoryOutLotRepositoryCustom {

    /**
     * 출고 헤더 ID 기준 LOT 아이템 목록 조회.
     *
     * @param outId 출고 헤더 PK
     * @return LOT 아이템 목록
     */
    List<InventoryOutLot> findByOutId(Long outId);
}
