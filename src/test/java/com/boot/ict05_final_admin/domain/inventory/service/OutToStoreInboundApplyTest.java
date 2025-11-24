package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOut;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.StoreInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.StoreMaterial;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HQ 출고 → (store, material) 재고 가산 테스트
 * - Repository 쓰기 메서드 없이 EntityManager 로만 처리
 * - 전제: store_material / store_inventory 존재 (1단계 시딩 완료 상태)
 */
@SpringBootTest
class OutToStoreInboundApplyTest {

    @Autowired private EntityManager em;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final java.math.BigDecimal ZERO_S3 = new java.math.BigDecimal("0.000");

    @Test
    @Commit
    @Transactional
    @DisplayName("본사 출고분을 가맹점 재고로 가산 (EntityManager 버전)")
        // @Disabled("1회 시딩 완료")
    void applyHqOutToStoreInventory_byEntityManager() {
        LocalDateTime cutoff = LocalDateTime.now(ZONE).minusYears(2);

        // 1) 최근 2년, store 지정된 출고만 로드
        TypedQuery<InventoryOut> q = em.createQuery(
                "select o from InventoryOut o " +
                        "left join fetch o.store s " +
                        "left join fetch o.material m " +
                        "where o.outDate >= :cutoff and o.store is not null", InventoryOut.class);
        q.setParameter("cutoff", cutoff);
        List<InventoryOut> outs = q.getResultList();
        assertThat(outs).isNotEmpty();

        // ... 클래스/어노테이션 동일

        for (InventoryOut out : outs) {
            var store = out.getStore();
            var material = out.getMaterial();

            // LOT 합계
            var lots = em.createQuery(
                    "select l from InventoryOutLot l join fetch l.batch b where l.out.id = :outId order by l.id asc",
                    InventoryOutLot.class
            ).setParameter("outId", out.getId()).getResultList();

            var lotSum = lots.stream().map(InventoryOutLot::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 헤더수량=LOT합 검증
            assertThat(lotSum.compareTo(out.getQuantity())).isZero();

            // ★ 없으면 생성 방식으로 변경
            StoreMaterial sm = getOrCreateStoreMaterial(em, store.getId(), material.getId());
            StoreInventory inv = getOrCreateStoreInventory(em, store.getId(), sm.getId());

            // 가산 + 상태 동기화
            inv.setQuantity(inv.getQuantity().add(lotSum));
            inv.touchAfterQuantityChange();
            em.merge(inv);
        }

    }



    // ===== helpers =====

    // 없으면 생성: StoreMaterial
    private com.boot.ict05_final_admin.domain.inventory.entity.StoreMaterial
    getOrCreateStoreMaterial(jakarta.persistence.EntityManager em, Long storeId, Long materialId) {
        var sm = em.createQuery(
                        "select sm from StoreMaterial sm where sm.store.id=:sid and sm.material.id=:mid",
                        com.boot.ict05_final_admin.domain.inventory.entity.StoreMaterial.class)
                .setParameter("sid", storeId)
                .setParameter("mid", materialId)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (sm != null) return sm;

        var storeRef = em.getReference(com.boot.ict05_final_admin.domain.store.entity.Store.class, storeId);
        var materialRef = em.getReference(com.boot.ict05_final_admin.domain.inventory.entity.Material.class, materialId);

        // NOT NULL 가능성 있는 컬럼들 기본값 방어
        String baseUnit = materialRef.getBaseUnit() != null ? materialRef.getBaseUnit() : "ea";
        String salesUnit = materialRef.getSalesUnit() != null ? materialRef.getSalesUnit() : baseUnit;
        Integer conv = (materialRef.getConversionRate() != null && materialRef.getConversionRate() > 0)
                ? materialRef.getConversionRate() : 100;

        sm = com.boot.ict05_final_admin.domain.inventory.entity.StoreMaterial.builder()
                .store(storeRef)
                .material(materialRef)
                .code(materialRef.getCode())
                .name(materialRef.getName())
                .category(materialRef.getMaterialCategory() != null ? materialRef.getMaterialCategory().name() : null)
                .baseUnit(baseUnit)
                .salesUnit(salesUnit)
                .supplier(null)
                .temperature(materialRef.getMaterialTemperature())
                .status(com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus.STOP)
                .optimalQuantity(null)
                .purchasePrice(null)
                .isHqMaterial(true)
                // ★ 중요: NOT NULL 컬럼 방어
                .quantity(ZERO_S3)                    // store_material_quantity = 0.000
                .sellingPrice(null)                   // nullable이면 그대로 null
                .expirationDate(null)                 // nullable이면 그대로 null
                .build();

        em.persist(sm);
        return sm;
    }

    // 없으면 생성: StoreInventory
    private com.boot.ict05_final_admin.domain.inventory.entity.StoreInventory
    getOrCreateStoreInventory(jakarta.persistence.EntityManager em, Long storeId, Long storeMaterialId) {
        var inv = em.createQuery(
                        "select si from StoreInventory si where si.store.id=:sid and si.storeMaterial.id=:smid",
                        com.boot.ict05_final_admin.domain.inventory.entity.StoreInventory.class)
                .setParameter("sid", storeId)
                .setParameter("smid", storeMaterialId)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (inv != null) return inv;

        var storeRef = em.getReference(com.boot.ict05_final_admin.domain.store.entity.Store.class, storeId);
        var smRef = em.getReference(com.boot.ict05_final_admin.domain.inventory.entity.StoreMaterial.class, storeMaterialId);

        inv = com.boot.ict05_final_admin.domain.inventory.entity.StoreInventory.builder()
                .store(storeRef)
                .storeMaterial(smRef)
                .quantity(ZERO_S3) // 초기 0.000
                .optimalQuantity(null)
                .status(com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus.SUFFICIENT)
                .build();

        inv.touchAfterQuantityChange();
        em.persist(inv);
        return inv;
    }

}
