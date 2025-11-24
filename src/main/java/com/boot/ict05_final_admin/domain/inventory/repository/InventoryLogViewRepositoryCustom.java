package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * v_inventory_log 커스텀 조회.
 */
public interface InventoryLogViewRepositoryCustom {

    /**
     * 재료/유형/기간 필터 기반 페이징 조회.
     *
     * @param materialId 재료 ID(옵션)
     * @param type       로그 유형(옵션, null/blank=전체)
     * @param startDate  시작일(옵션, 포함)
     * @param endDate    종료일(옵션, 포함)
     * @param pageable   페이징/정렬
     */
    Page<InventoryLogView> findLogsByFilter(Long materialId,
                                            String type,
                                            LocalDate startDate,
                                            LocalDate endDate,
                                            Pageable pageable);
}
