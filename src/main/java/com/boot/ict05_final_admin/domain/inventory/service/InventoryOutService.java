package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryOutPreviewItemDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.*;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchQueryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutLotRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutRepository;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderDetailDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderItemDTO;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 출고 도메인 서비스.
 *
 * <p>
 * FIFO 기반 출고 분할 미리보기와 출고 확정을 담당한다.
 * 조회는 QueryRepository에 위임한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class InventoryOutService {

    private final InventoryBatchQueryRepository inventoryBatchQueryRepository;
    private final InventoryOutRepository inventoryOutRepository;
    private final InventoryOutLotRepository inventoryOutLotRepository;
    private final InventoryBatchRepository inventoryBatchRepository;

    private final EntityManager em;

    private final UnitPriceService unitPriceService;
    private final InventoryService inventoryService;

    /**
     * 출고(FIFO) 분할 미리보기.
     *
     * <p>
     * 현재고를 확인하여 부족 시 예외를 던지고, 사용 가능한 배치 목록을
     * 입고순(오래된 순)으로 순회하며 요청 수량을 소진할 때까지 슬라이싱한다.
     * </p>
     *
     * @param materialId 재료 ID
     * @param qty        총 출고 수량(양수)
     * @return 배치별 출고량 계획 목록
     * @throws IllegalArgumentException 현재고가 부족할 때
     */
    @Transactional(readOnly = true)
    public List<InventoryOutPreviewItemDTO> previewFifo(final Long materialId, final BigDecimal qty) {
        BigDecimal currentStock = inventoryService.hqRemainOfMaterial(materialId);
        if (currentStock == null) currentStock = BigDecimal.ZERO;

        if (currentStock.compareTo(qty) < 0) {
            throw new IllegalArgumentException("주문 수량이 현재 재고를 초과합니다. 현재 재고: "
                    + currentStock + ", 주문 수량: " + qty);
        }

        var candidates = inventoryBatchQueryRepository.findAvailableBatchesForFifo(materialId);
        var remain = qty;
        List<InventoryOutPreviewItemDTO> plan = new ArrayList<>();

        for (var c : candidates) {
            if (remain.signum() <= 0) break;
            var take = c.getAvailable().min(remain);
            if (take.signum() > 0) {
                plan.add(InventoryOutPreviewItemDTO.builder()
                        .batchId(c.getBatchId())
                        .lotNo(c.getLotNo())
                        .qty(take)
                        .expirationDate(c.getExpirationDate())
                        .build());
                remain = remain.subtract(take);
            }
        }

        return plan;
    }

    /**
     * 본사 재고 현재고 조회(위임).
     *
     * @param materialId 재료 ID
     * @return 현재고(없으면 0 가능)
     */
    @Transactional(readOnly = true)
    public BigDecimal hqRemainOfMaterial(final Long materialId) {
        return inventoryService.hqRemainOfMaterial(materialId);
    }

    /**
     * 출고 단가 결정 규칙.
     *
     * <ol>
     *   <li>기준 시점(ts)의 최신 매입단가(UnitPriceType.PURCHASE)</li>
     *   <li>없으면 최신 출고 헤더의 단가</li>
     *   <li>그래도 없으면 0</li>
     * </ol>
     *
     * @param materialId 재료 ID
     * @param ts         기준 시점
     * @return 결정된 단가(없으면 0)
     */
    private BigDecimal resolveOutUnitPrice(final Long materialId, final LocalDateTime ts) {
        BigDecimal fromUnitPriceTable = unitPriceService
                .latestPurchasePrice(materialId, ts)
                .map(UnitPrice::getPurchasePrice)
                .orElse(null);

        if (fromUnitPriceTable != null && fromUnitPriceTable.signum() > 0) {
            return fromUnitPriceTable;
        }

        BigDecimal fromLastOut = inventoryOutRepository
                .findTopByMaterial_IdOrderByOutDateDescIdDesc(materialId)
                .map(InventoryOut::getUnitPrice)
                .orElse(null);

        if (fromLastOut != null && fromLastOut.signum() > 0) {
            return fromLastOut;
        }

        return BigDecimal.ZERO;
    }

    /**
     * 출고 확정.
     *
     * <p>
     * 검증 → FIFO 분할 → 배치 차감 → 현재고 재계산 → 단가 확정/단가이력 기록 →
     * 출고 헤더/LOT 생성 → 인벤토리 수량 동기화 순으로 처리한다.
     * </p>
     *
     * @param materialId 재료 ID
     * @param storeId    가맹점 ID(선택)
     * @param totalQty   총 출고 수량(양수)
     * @param outDate    출고 일시(없으면 now)
     * @param memo       비고
     * @return 생성된 출고 헤더 ID
     * @throws IllegalArgumentException 현재고 부족 또는 수량 오류 시
     * @throws IllegalStateException    FIFO 합계 불일치 등 내부 상태 오류 시
     */
    @Transactional
    public Long confirmOut(final Long materialId,
                           final Long storeId,
                           final BigDecimal totalQty,
                           final LocalDateTime outDate,
                           final String memo) {

        if (totalQty == null || totalQty.signum() <= 0) {
            throw new IllegalArgumentException("출고 수량이 0 이하입니다.");
        }

        final LocalDateTime ts = (outDate != null) ? outDate : LocalDateTime.now();

        // 1) 현재 HQ 재고 검증
        BigDecimal currentStock = inventoryService.hqRemainOfMaterial(materialId);
        if (currentStock == null) currentStock = BigDecimal.ZERO;
        if (currentStock.compareTo(totalQty) < 0) {
            throw new IllegalArgumentException("출고 수량이 현재고를 초과합니다. current="
                    + currentStock + ", out=" + totalQty);
        }

        // 2) FIFO plan 생성
        List<InventoryOutPreviewItemDTO> plan = previewFifo(materialId, totalQty);

        BigDecimal plannedSum = BigDecimal.ZERO;
        for (InventoryOutPreviewItemDTO p : plan) {
            plannedSum = plannedSum.add(p.getQty());

            // 배치 조회 + 차감 + 저장
            InventoryBatch batch = inventoryBatchRepository.findById(p.getBatchId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("배치를 찾을 수 없습니다. id=" + p.getBatchId()));

            batch.subtractQuantity(p.getQty());
            inventoryBatchRepository.save(batch); // @DynamicUpdate 로 quantity만 UPDATE
        }

        if (plannedSum.compareTo(totalQty) != 0) {
            throw new IllegalStateException("FIFO 분할 합계가 요청 수량과 일치하지 않습니다. planned="
                    + plannedSum + ", requested=" + totalQty);
        }

        // 3) 배치 차감 이후 현재고 재계산
        BigDecimal remain = inventoryService.hqRemainOfMaterial(materialId);

        // 4) 단가 결정 및 단가 이력 기록(출고가=매입가 정책)
        BigDecimal unitPrice = resolveOutUnitPrice(materialId, ts);
        unitPriceService.addPricesForMaterial(materialId, unitPrice, unitPrice);

        // 5) 출고 헤더 생성
        InventoryOut out = InventoryOut.builder()
                .material(em.getReference(Material.class, materialId))
                .store(storeId != null ? em.getReference(Store.class, storeId) : null)
                .outDate(ts)
                .quantity(totalQty)
                .stockAfter(remain)
                .unitPrice(unitPrice)
                .memo(memo)
                .build();
        out = inventoryOutRepository.save(out);

        // 6) LOT 생성(미리보기 계획 기준)
        for (InventoryOutPreviewItemDTO p : plan) {
            InventoryOutLot lot = InventoryOutLot.builder()
                    .out(out)
                    .batch(em.getReference(InventoryBatch.class, p.getBatchId()))
                    .quantity(p.getQty())
                    .build();
            inventoryOutLotRepository.save(lot);
        }

        // 7) inventory 테이블 현재고 동기화
        inventoryService.syncInventoryQuantity(materialId, remain);

        return out.getId();
    }

    /**
     * 수주 기반 본사→가맹점 출고 생성.
     *
     * <p>
     * 수주 상세({@link ReceiveOrderDetailDTO})의 품목 리스트를 순회하며
     * 자재별로 {@link #confirmOut(Long, Long, BigDecimal, LocalDateTime, String)} 를 호출한다.
     * 여러 자재가 포함된 경우 여러 개의 출고 헤더가 생성될 수 있으며,
     * 이 메서드는 그 중 첫 번째 헤더를 반환한다(핵심 효과는 재고 차감 및 로그 생성).
     * </p>
     *
     * @param orderDetail 수주 상세 DTO(헤더+아이템 포함)
     * @return 생성된 출고 헤더 중 첫 번째
     * @throws IllegalArgumentException 입력이 null 일 때
     * @throws IllegalStateException    품목이 없거나 재료 ID 누락 등 데이터 오류일 때
     */
    @Transactional
    public InventoryOut createOutByReceiveOrder(final ReceiveOrderDetailDTO orderDetail) {
        if (orderDetail == null) {
            throw new IllegalArgumentException("수주 상세 정보가 null 입니다.");
        }

        List<ReceiveOrderItemDTO> items = orderDetail.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("수주 상세 품목이 없습니다. orderId=" + orderDetail.getId()
                    + ", orderCode=" + orderDetail.getOrderCode());
        }

        final Long storeId = orderDetail.getStoreId();
        final String baseMemo = "수주 자동 출고: " + orderDetail.getOrderCode();
        final LocalDateTime outDate = LocalDateTime.now();

        InventoryOut firstOut = null;

        for (ReceiveOrderItemDTO item : items) {
            if (item == null) continue;

            Integer cnt = item.getDetailCount();
            if (cnt == null || cnt <= 0) continue;

            Long materialId = item.getMaterialId();
            if (materialId == null) {
                throw new IllegalStateException(
                        "수주 상세 품목에 재료 ID가 없습니다. orderId=" + orderDetail.getId()
                                + ", orderCode=" + orderDetail.getOrderCode()
                                + ", itemName=" + item.getName()
                );
            }

            BigDecimal qty = BigDecimal.valueOf(cnt.longValue());
            Long outId = confirmOut(materialId, storeId, qty, outDate, baseMemo);

            if (firstOut == null) {
                firstOut = inventoryOutRepository.findById(outId)
                        .orElseThrow(() ->
                                new IllegalStateException("출고 헤더를 찾을 수 없습니다. id=" + outId));
            }
        }

        if (firstOut == null) {
            throw new IllegalStateException(
                    "출고 대상 수량이 없습니다. orderId=" + orderDetail.getId()
                            + ", orderCode=" + orderDetail.getOrderCode()
            );
        }

        return firstOut;
    }

    /**
     * 출고 헤더 삭제.
     *
     * @param outId 출고 헤더 ID
     */
    @Transactional
    public void deleteOut(final Long outId) {
        inventoryOutRepository.deleteById(outId);
    }
}
