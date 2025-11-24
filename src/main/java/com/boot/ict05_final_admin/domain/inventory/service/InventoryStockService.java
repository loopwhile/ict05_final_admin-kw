package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.Inventory;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 본사 재고 수량 관리 서비스.
 *
 * <p>
 * 재고 수량 증액 시 행이 없으면 생성하고(초기 0), 이후 수량 증가와 함께
 * 상태/갱신일시는 엔티티 내부 메서드로 동기화한다.
 * 동시성 제어를 위해 재고 행 조회 시 PESSIMISTIC_WRITE 잠금을 사용한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class InventoryStockService {

    private final InventoryRepository inventoryRepository;
    private final EntityManager em;

    /**
     * 본사 재고 수량을 증가시킨다(없으면 생성 후 0에서 시작).
     *
     * <p>
     * - 대상 재료의 재고 행을 PESSIMISTIC_WRITE로 잠금<br>
     * - 행이 없으면 생성(초기 {@code quantity=0}, {@code optimalQuantity=재료.optimalQuantity})<br>
     * - {@code delta}만큼 가산 후 상태/갱신일시는 {@code touchAfterQuantityChange()}로 반영
     * </p>
     *
     * @param materialId 재료 ID
     * @param delta      증가시킬 수량(양수 필수)
     * @return 증가 적용 후 수량(after)
     * @throws IllegalArgumentException {@code delta}가 null이거나 0 이하일 때
     */
    @Transactional
    public BigDecimal addToInventory(Long materialId, BigDecimal delta) {
        if (delta == null || delta.signum() <= 0) {
            throw new IllegalArgumentException("delta must be > 0");
        }

        // PESSIMISTIC_WRITE로 행 잠금. 없으면 생성.
        Inventory inv = inventoryRepository.findByMaterialIdForUpdate(materialId)
                .orElseGet(() -> {
                    Material mRef = em.getReference(Material.class, materialId);
                    Inventory created = Inventory.builder()
                            .material(mRef)
                            .quantity(BigDecimal.ZERO)
                            // 초기 적정수량을 재료값으로 동기화하려면 유지
                            .optimalQuantity(mRef.getOptimalQuantity())
                            .build();
                    return inventoryRepository.saveAndFlush(created); // 즉시 생성하여 잠금 컨텍스트 포함
                });

        BigDecimal before = inv.getQuantity() == null ? BigDecimal.ZERO : inv.getQuantity();
        BigDecimal after = before.add(delta);
        inv.setQuantity(after);
        inv.touchAfterQuantityChange(); // 상태 + updateDate 동기화

        // Flush는 트랜잭션 종료 시점에 위임
        return after;
    }
}
