package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryLogDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryLogViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 재고 로그 뷰 서비스.
 *
 * <p>
 * 화면용 집계/뷰 테이블(v_inventory_log)의 페이징 조회를 제공한다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryLogViewService {

    private final InventoryLogViewRepository inventoryLogViewRepository;

    /**
     * 재료/유형/기간 조건으로 로그를 페이징 조회한다.
     *
     * <p>
     * 리포지토리에서 {@link InventoryLogView} 페이지를 조회한 뒤,
     * 화면/엑셀 공용 DTO({@link InventoryLogDTO})로 매핑하여 반환한다.
     * </p>
     *
     * @param materialId 재료 ID(옵션)
     * @param type       로그 유형(옵션, 예: IN/OUT/ADJUST 등)
     * @param startDate  시작일(옵션, 포함)
     * @param endDate    종료일(옵션, 포함)
     * @param pageable   페이지/정렬 파라미터
     * @return 매핑된 DTO 페이지
     */
    @Transactional(readOnly = true)
    public Page<InventoryLogDTO> getFilteredLogs(final Long materialId,
                                                 final String type,
                                                 final LocalDate startDate,
                                                 final LocalDate endDate,
                                                 final Pageable pageable) {

        Page<InventoryLogView> page =
                inventoryLogViewRepository.findLogsByFilter(materialId, type, startDate, endDate, pageable);

        List<InventoryLogDTO> dtoList = page.getContent().stream()
                .map(this::toDto)
                .toList();

        // 디버그 로그(필요 시 레벨 조정)
        dtoList.forEach(d ->
                log.info("LOG DTO => id={}, type={}, qty={}", d.getLogId(), d.getLogType(), d.getQuantity())
        );

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    /**
     * v_inventory_log 행을 화면/엑셀 공용 DTO로 변환한다.
     *
     * <p>
     * {@code logType}은 재계산하지 않고 뷰의 값을 그대로 사용한다.
     * </p>
     *
     * @param row 뷰 엔티티
     * @return 변환된 DTO
     */
    private InventoryLogDTO toDto(final InventoryLogView row) {
        return InventoryLogDTO.builder()
                .logId(row.getRowId())
                .logDate(row.getDate())
                .logType(row.getType())
                .quantity(row.getQuantity())
                .stockAfter(row.getStockAfter())
                .unitPrice(row.getUnitPrice())
                .memo(row.getMemo())
                .storeId(row.getStoreId())
                .storeName(row.getStoreName())
                .batchId(row.getBatchId())
                .build();
    }
}
