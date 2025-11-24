package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.StoreInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.StoreMaterial;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import com.boot.ict05_final_admin.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
class InventoryOut_MonthEndClearanceWithInboundTest {

    @Autowired private InventoryOutService inventoryOutService;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private EntityManager em;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final BigDecimal ZERO_S3 = new BigDecimal("0.000");
    private final Random rnd = new Random();

    /**
     * 월말 정리 출고 + 즉시 가맹점 입고(재고 가산)까지 한 번에 처리
     * - 기간: 최근 24개월의 각 월 말일 풀에서 랜덤 선택
     * - 대상: 가맹점별 모든 재료, 재료당 1~3회
     * - 수량: 회차마다 HQ 현재고의 5~10%
     * - 매장 배정: store 리스트 순회(라운드로빈 효과)
     */
    @Test
    @Commit
    @Transactional
    @DisplayName("월말 정리 출고(본사→가맹점) + 즉시 가맹점 입고 반영")
    @Disabled("1회 시딩 완료")
    void monthEnd_clearance_with_inbound_apply() {
        // 1) 실존 매장/재료
        List<Store> stores = storeRepository.findAll();
        if (stores.isEmpty()) return;

        List<Material> materials = materialRepository.findAll();
        if (materials.isEmpty()) return;

        // 2) 최근 24개월 월말 일자 풀
        List<LocalDateTime> monthEnds = monthEndPool(24);

        // 3) 매장 라운드로빈 순회 → 매장별 모든 재료 처리
        for (Store store : stores) {
            for (Material m : materials) {
                int times = randBetween(1, 3);
                for (int i = 0; i < times; i++) {
                    // 남은 HQ 재고 재조회
                    BigDecimal remain = nz(inventoryOutService.hqRemainOfMaterial(m.getId()));
                    if (remain.signum() <= 0) break;

                    // 수량: 현재고의 5~10% (소수 3자리 고정, 최소 보장)
                    int pct = randBetween(5, 10);
                    BigDecimal qty = remain
                            .multiply(BigDecimal.valueOf(pct))
                            .divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP);
                    BigDecimal min = remain.compareTo(BigDecimal.ONE) >= 0 ? BigDecimal.ONE : new BigDecimal("0.001");
                    qty = qty.max(min);
                    if (qty.compareTo(remain) > 0) qty = remain;
                    qty = qty.setScale(3, RoundingMode.HALF_UP);

                    // 출고 일시: 월말 + 업무시간(09~18시) 랜덤 시:분:초
                    LocalDateTime outDate = withBusinessTime(monthEnds.get(rnd.nextInt(monthEnds.size())));

                    // 4) 본사→가맹점 출고 생성 (FIFO 차감/단가/LOT/재고동기화 포함)
                    Long outId = inventoryOutService.confirmOut(
                            m.getId(),
                            store.getId(),
                            qty,
                            outDate,
                            "seed: month-end clearance"
                    );

                    // 5) 방금 출고한 LOT 합계를 해당 (store, material)의 재고에 즉시 가산 → "입고 효과"
                    List<InventoryOutLot> lots = em.createQuery(
                                    "select l from InventoryOutLot l join fetch l.batch b where l.out.id = :outId order by l.id asc",
                                    InventoryOutLot.class)
                            .setParameter("outId", outId)
                            .getResultList();

                    BigDecimal lotSum = lots.stream()
                            .map(InventoryOutLot::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // (store, material) 매핑/재고 확보(없으면 생성)
                    StoreMaterial sm = getOrCreateStoreMaterial(store.getId(), m.getId());
                    StoreInventory inv = getOrCreateStoreInventory(store.getId(), sm.getId());

                    // 가산 + 상태 동기화
                    inv.setQuantity(inv.getQuantity().add(lotSum));
                    inv.touchAfterQuantityChange();
                    em.merge(inv);
                }
            }
        }
    }

    // ===== helpers =====

    private List<LocalDateTime> monthEndPool(int monthsBack) {
        List<LocalDateTime> list = new ArrayList<>(monthsBack);
        YearMonth base = YearMonth.now(KST);
        for (int i = 0; i < monthsBack; i++) {
            YearMonth ym = base.minusMonths(i);
            LocalDate end = ym.atEndOfMonth();
            list.add(end.atTime(12, 0)); // 기본 정오(후속에서 업무시간으로 보정)
        }
        return list;
    }

    private LocalDateTime withBusinessTime(LocalDateTime base) {
        int hour = randBetween(9, 18);
        int minute = rnd.nextInt(60);
        int second = rnd.nextInt(60);
        return base.withHour(hour).withMinute(minute).withSecond(second);
    }

    private int randBetween(int minInclusive, int maxInclusive) {
        return minInclusive + rnd.nextInt(maxInclusive - minInclusive + 1);
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    /**
     * (store, material) 매핑이 없으면 생성
     * - NOT NULL 가능 필드 기본값 방어
     * - code 유니크 전역일 가능성 대비: materialCode + "-S" + storeId
     */
    private StoreMaterial getOrCreateStoreMaterial(Long storeId, Long materialId) {
        // 조회
        TypedQuery<StoreMaterial> q = em.createQuery(
                "select sm from StoreMaterial sm where sm.store.id=:sid and sm.material.id=:mid",
                StoreMaterial.class);
        q.setParameter("sid", storeId).setParameter("mid", materialId);
        StoreMaterial found = q.getResultStream().findFirst().orElse(null);
        if (found != null) return found;

        // 참조
        Store storeRef = em.getReference(Store.class, storeId);
        Material materialRef = em.getReference(Material.class, materialId);

        // 기본값 방어
        String baseUnit = materialRef.getBaseUnit() != null ? materialRef.getBaseUnit() : "ea";
        String salesUnit = materialRef.getSalesUnit() != null ? materialRef.getSalesUnit() : baseUnit;

        // 새 매핑 생성
        StoreMaterial sm = StoreMaterial.builder()
                .store(storeRef)
                .material(materialRef)
                .code(safeCode(materialRef.getCode(), storeId))
                .name(materialRef.getName())
                .category(materialRef.getMaterialCategory() != null ? materialRef.getMaterialCategory().name() : null)
                .baseUnit(baseUnit)
                .salesUnit(salesUnit)
                .supplier(null)
                .temperature(materialRef.getMaterialTemperature())
                .status(MaterialStatus.STOP)
                .optimalQuantity(null)
                .purchasePrice(null)
                .isHqMaterial(true)
                // 중요: 일부 스키마에서 NOT NULL
                .quantity(ZERO_S3)
                .sellingPrice(null)
                .expirationDate(null)
                .build();

        em.persist(sm);
        return sm;
    }

    private String safeCode(String materialCode, Long storeId) {
        String base = (materialCode != null && !materialCode.isBlank()) ? materialCode : "MAT";
        return base + "-S" + storeId; // 전역 유니크 대비
    }

    /**
     * (store, storeMaterial) 재고 없으면 0으로 생성
     */
    private StoreInventory getOrCreateStoreInventory(Long storeId, Long storeMaterialId) {
        TypedQuery<StoreInventory> q = em.createQuery(
                "select si from StoreInventory si where si.store.id=:sid and si.storeMaterial.id=:smid",
                StoreInventory.class);
        q.setParameter("sid", storeId).setParameter("smid", storeMaterialId);
        StoreInventory inv = q.getResultStream().findFirst().orElse(null);
        if (inv != null) return inv;

        Store storeRef = em.getReference(Store.class, storeId);
        StoreMaterial smRef = em.getReference(StoreMaterial.class, storeMaterialId);

        inv = StoreInventory.builder()
                .store(storeRef)
                .storeMaterial(smRef)
                .quantity(ZERO_S3)
                .optimalQuantity(null)
                .status(InventoryStatus.SUFFICIENT)
                .build();

        inv.touchAfterQuantityChange();
        em.persist(inv);
        return inv;
    }
}
