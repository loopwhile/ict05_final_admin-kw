package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;

import java.util.List;

/**
 * 배치(LOT) 커스텀 리포지토리.
 *
 * <p>HQ 배치 목록 및 FIFO 후보 조회.</p>
 */
public interface InventoryBatchRepositoryCustom {

    /** HQ 배치: 재료별, 잔량>0, 유통기한↑→입고일↑, 만료 NULL 후순위 */
    List<InventoryBatch> findHqBatchesForMaterial(Long materialId);

    /** FIFO 후보: 재료별, 잔량>0, 유통기한↑→입고일↑ */
    List<InventoryBatch> findFifoCandidates(Long materialId);
}
