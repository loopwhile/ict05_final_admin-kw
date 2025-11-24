package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryAdjustDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.*;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryAdjustmentRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.UnitPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.boot.ict05_final_admin.domain.inventory.utility.InventoryLogIdUtil.unwrap;

/**
 * 재고 조정 도메인 서비스.
 *
 * <p>
 * 대상은 <b>재고 PK(inventory.id)</b> 기준이며, Δ(증감치)는 {@code difference}를 사용한다.
 * 조정 후 수량은 0 미만이 될 수 없으며, 배치(FIFO) 차감 시 배치 잔량이 부족하면 예외를 던져
 * 인벤토리 테이블과 배치 합계 간의 불일치를 방지한다.
 * </p>
 *
 * <p>
 * 단가는 기준 시점의 최신 매입단가를 조회하여 적용한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryAdjustmentService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final InventoryRepository inventoryRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryAdjustmentRepository inventoryAdjustmentRepository;
    private final UnitPriceRepository unitPriceRepository;

    /**
     * 기준 시점(at)의 최신 매입단가 조회.
     *
     * @param materialId 재료 ID
     * @param at         기준 시점
     * @return 최신 매입단가(없으면 0)
     */
    private BigDecimal getLatestPurchasePrice(final Long materialId, final LocalDateTime at) {
        return unitPriceRepository.findLatestPurchasePrice(materialId, at)
                .map(UnitPrice::getPurchasePrice)
                .orElse(ZERO);
    }

    /**
     * 본사 재고 수량을 직접 조정한다.
     *
     * <ol>
     *   <li>대상 재고 조회 및 before/after/diff 계산</li>
     *   <li>인벤토리 수량 업데이트(상태/갱신일시 동기화)</li>
     *   <li>diff &gt; 0: ADJ 배치 생성(증가분만큼 단일 LOT)</li>
     *   <li>diff &lt; 0: FIFO로 배치 차감(부족 시 예외)</li>
     *   <li>조정 로그 기록(단가=기준 시점 최신 매입단가)</li>
     * </ol>
     *
     * @param dto 재고 수량 조정 요청 DTO
     */
    @Transactional
    public void adjustInventory(final InventoryAdjustDTO dto) {
        log.info("[ADJUST_REQ] dto={}", dto);

        // 1) 대상 재고
        Inventory inventory = inventoryRepository.findById(dto.getInventoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 재고 정보를 찾을 수 없습니다. id=" + dto.getInventoryId()));

        Material material = inventory.getMaterial();

        // 2) before/after/diff
        BigDecimal before = inventory.getQuantity() != null ? inventory.getQuantity() : ZERO;
        BigDecimal after = dto.getQuantityAfter();
        if (after == null) {
            throw new IllegalArgumentException("조정 후 수량(quantityAfter)은 필수입니다.");
        }
        if (after.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("조정 후 수량이 0 미만일 수 없습니다. quantityAfter=" + after);
        }

        BigDecimal diff = after.subtract(before);
        dto.setQuantityBefore(before);
        dto.setDifference(diff);

        // 3) 인벤토리 수량 반영 + 상태/업데이트 일시 동기화
        inventory.setQuantity(after);
        inventory.touchAfterQuantityChange(); // InventoryBase 공통 메서드
        inventoryRepository.saveAndFlush(inventory);

        log.info("[ADJUST_DONE] invId={}, before={}, after={}, diff={}",
                inventory.getId(), before, after, diff);

        // 기준 시점(일관성 유지를 위해 한 번만 산출)
        final LocalDateTime now = LocalDateTime.now();
        final BigDecimal latestPrice = getLatestPurchasePrice(material.getId(), now);

        // 4) 배치 처리
        if (diff.compareTo(ZERO) > 0) {
            // 수량 증가 → ADJ 배치 1건 생성
            String lotNo = String.format("%s-%s-ADJ",
                    material.getCode(),
                    now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            InventoryBatch batch = InventoryBatch.builder()
                    .material(material)
                    .store(null) // 본사 배치
                    .lotNo(lotNo)
                    .receivedDate(now)
                    .expirationDate(null)
                    .receivedQuantity(diff)
                    .quantity(diff)              // 잔량 = 입고수량
                    .unitPrice(latestPrice)
                    .createdAt(now)
                    .build();

            inventoryBatchRepository.saveAndFlush(batch);

        } else if (diff.compareTo(ZERO) < 0) {
            // 수량 감소 → FIFO 차감
            BigDecimal remaining = diff.abs();

            List<InventoryBatch> batches = inventoryBatchRepository.findFifoCandidates(material.getId());
            for (InventoryBatch b : batches) {
                if (remaining.compareTo(ZERO) <= 0) break;

                BigDecimal avail = b.getQuantity();
                if (avail == null || avail.compareTo(ZERO) <= 0) continue;

                BigDecimal used = remaining.min(avail);
                b.setQuantity(avail.subtract(used));
                inventoryBatchRepository.save(b);

                remaining = remaining.subtract(used);
            }

            // 배치 잔량이 부족하면 불일치 방지 위해 예외
            if (remaining.compareTo(ZERO) > 0) {
                throw new IllegalStateException(
                        "배치 잔량이 부족해 FIFO 차감에 실패했습니다. 부족수량=" + remaining
                );
            }
        }

        // 5) 조정 로그 기록(단가: 기준 시점 최신 매입단가)
        InventoryAdjustment adj = InventoryAdjustment.builder()
                .inventory(inventory)
                .quantityBefore(before)
                .quantityAfter(after)
                .difference(diff)
                .unitPrice(latestPrice)
                .memo(dto.getMemo())
                .reason(dto.getReason())
                .createdAt(now)
                .build();

        inventoryAdjustmentRepository.save(adj);

        // 6) 로그
        log.info("[INVENTORY ADJUST] material={}, diff={}, before={}, after={}, reason={}",
                material.getCode(), diff, before, after, dto.getReason());
    }

    /**
     * 재고 조정 상세 조회.
     *
     * @param logId inventory_adjustment.adjustment_id(뷰에서 전달되는 logId는 래핑되어 올 수 있음)
     * @return 조정 상세 DTO
     */
    @Transactional(readOnly = true)
    public InventoryAdjustDTO getAdjustDetail(final Long logId) {
        long pk = unwrap(logId); // 예: 2000000001 → 1

        InventoryAdjustment adj = inventoryAdjustmentRepository.findById(pk)
                .orElseThrow(() ->
                        new IllegalArgumentException("재고 조정 이력을 찾을 수 없습니다. id=" + logId));

        return InventoryAdjustDTO.builder()
                .inventoryId(adj.getInventory().getId())
                .material(adj.getInventory().getMaterial().getId())
                .quantityBefore(adj.getQuantityBefore())
                .quantityAfter(adj.getQuantityAfter())
                .difference(adj.getDifference())
                .reason(adj.getReason())
                .memo(adj.getMemo())
                .build();
    }
}
