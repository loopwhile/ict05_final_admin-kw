package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.BatchOutRowDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 출고-LOT 커스텀 쿼리 인터페이스.
 */
public interface InventoryOutLotQueryRepositoryCustom {

    /**
     * 특정 배치의 출고 이력을 페이징 조회한다.
     *
     * @param batchId  배치(LOT) PK
     * @param pageable 페이징/정렬
     * @return 출고 이력 DTO 페이지
     */
    Page<BatchOutRowDTO> pageOutHistoryByBatch(Long batchId, Pageable pageable);
}
