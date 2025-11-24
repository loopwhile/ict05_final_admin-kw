package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryLotDetailDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 본사 재고 배치(LOT) 조회 서비스.
 *
 * <p>
 * 화면에서 사용하는 배치 목록/상세 조회를 제공한다.
 * 모든 메서드는 읽기 전용 트랜잭션으로 수행된다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryBatchService {

    private final InventoryBatchRepository inventoryBatchRepository;

    /**
     * 재료별 배치 목록을 조회한다.
     *
     * <p>정렬 기준: 입고일(desc).</p>
     *
     * @param materialId 재료 ID(필수)
     * @return 해당 재료의 배치 목록(잔량 0 포함)
     * @throws IllegalArgumentException materialId가 null일 때
     */
    public List<InventoryBatch> getBatchesByMaterial(final Long materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("materialId is required");
        }
        return inventoryBatchRepository.findAllByMaterial_IdOrderByReceivedDateDesc(materialId);
    }

    /**
     * 단일 배치(LOT) 상세를 조회한다.
     *
     * @param batchId 배치 PK(필수)
     * @return LOT 상세 DTO
     * @throws IllegalArgumentException 배치를 찾을 수 없을 때
     */
    public InventoryLotDetailDTO getLotDetail(final Long batchId) {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId is required");
        }

        InventoryBatch b = inventoryBatchRepository.findById(batchId)
                .orElseThrow(() ->
                        new IllegalArgumentException("배치 정보를 찾을 수 없습니다. id=" + batchId));

        InventoryLotDetailDTO dto = new InventoryLotDetailDTO();
        dto.setBatchId(b.getId());
        dto.setMaterialId(b.getMaterial().getId());
        dto.setMaterialCode(b.getMaterial().getCode());
        dto.setMaterialName(b.getMaterial().getName());
        dto.setLotNo(b.getLotNo());
        dto.setReceivedDate(b.getReceivedDate());
        dto.setExpirationDate(b.getExpirationDate());
        dto.setReceivedQuantity(b.getReceivedQuantity());
        dto.setRemainingQuantity(b.getQuantity());
        dto.setUnitPrice(b.getUnitPrice());

        return dto;
    }
}
