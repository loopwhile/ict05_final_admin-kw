package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.BatchStatusRowDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.FifoCandidateDTO;

import java.util.List;

/**
 * 배치 조회/슬라이싱 커스텀 인터페이스.
 */
public interface InventoryBatchQueryRepositoryCustom {

    /**
     * 재료별 배치 현황(요약) 목록.
     */
    List<BatchStatusRowDTO> findBatchStatusByMaterial(Long materialId);

    /**
     * FIFO 출고를 위한 사용 가능 배치 목록(입고일 오름차순, 잔량>0).
     */
    List<FifoCandidateDTO> findAvailableBatchesForFifo(Long materialId);
}
