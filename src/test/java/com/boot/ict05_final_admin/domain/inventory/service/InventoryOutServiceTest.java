package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import com.boot.ict05_final_admin.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

@SpringBootTest
class InventoryOutServiceTest {

    @Autowired private InventoryOutService inventoryOutService;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private StoreRepository storeRepository;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final int YEARS_RANGE = 2;      // 최근 2년 랜덤
    private static final int MIN_TIMES = 3;        // 재료당 출고 최소 횟수
    private static final int MAX_TIMES = 7;        // 재료당 출고 최대 횟수
    private static final int STORE_MIN_ID = 1;     // 가맹점 ID 범위
    private static final int STORE_MAX_ID = 34;

    private static final int PCT_MIN = 10;         // 현재고의 10% ~
    private static final int PCT_MAX = 30;         // 현재고의 ~30%

    private final Random rnd = new Random();       // 필요 시 new Random(seed)로 고정 가능

    /**
     * 1회 실행 후 @Disabled 권장.
     */
    @Test
    @Commit
    @DisplayName("최근 2년 랜덤: 본사→가맹점 출고 시딩 (재료당 3~7회, FIFO 차감)")
    @Disabled("1회 시딩 완료")
    void seed_randomOutToStores_forAllMaterials() {

        List<Long> storeIds = storeRepository.findAll()
                .stream().map(Store::getId).toList();
        List<Material> materials = materialRepository.findAll();

        if (materials.isEmpty()) {
            throw new IllegalStateException("Material이 비어 있습니다. HQ 입고 데이터를 먼저 준비하세요.");
        }

        for (Material m : materials) {
            // 있는 가맹점만
            Long targetStoreId = storeIds.isEmpty()
                    ? null
                    : storeIds.get(randBetween(0, storeIds.size() - 1));

            // 재고가 있는 재료만 대상
            BigDecimal remain = nz(inventoryOutService.hqRemainOfMaterial(m.getId()));
            if (remain.signum() <= 0) continue;

            int times = randBetween(MIN_TIMES, MAX_TIMES);

            for (int i = 0; i < times; i++) {
                // 현재고 재조회 (직전 출고로 감소했을 수 있음)
                remain = nz(inventoryOutService.hqRemainOfMaterial(m.getId()));
                if (remain.signum() <= 0) break;

                // 출고 수량 = 현재고의 10~30% 랜덤 (최소 1 또는 0.001 보장)
                BigDecimal qty = pickQuantity(remain);

                // 출고 일시: 최근 2년 랜덤
                LocalDateTime outDate = randomDateTimeWithinYears(YEARS_RANGE);

                // 가맹점: 1~34 랜덤
                long storeId = randBetween(STORE_MIN_ID, STORE_MAX_ID);

                // 실행
                inventoryOutService.confirmOut(
                        m.getId(),
                        targetStoreId,   // ← 실존 storeId 또는 null
                        qty,
                        outDate,
                        "seed: rnd out"
                );
            }
        }
    }

    // ===== helpers =====

    private BigDecimal pickQuantity(BigDecimal remain) {
        // remain * (PCT_MIN..PCT_MAX)%
        int pct = randBetween(PCT_MIN, PCT_MAX);
        BigDecimal pctQty = remain
                .multiply(BigDecimal.valueOf(pct))
                .divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP);

        // 최소 보장 (정수/소수 혼용 안전장치)
        BigDecimal min = remain.compareTo(BigDecimal.ONE) >= 0
                ? BigDecimal.ONE
                : new BigDecimal("0.001");

        BigDecimal qty = pctQty.max(min);
        // 남은 수량을 넘어가지 않도록 보정
        if (qty.compareTo(remain) > 0) qty = remain;
        // 테스트/서비스가 소수 3자리 규칙을 선호하므로 스케일 고정
        return qty.setScale(3, RoundingMode.HALF_UP);
    }

    private int randBetween(int min, int max) {
        return min + rnd.nextInt(max - min + 1);
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private LocalDateTime randomDateTimeWithinYears(int years) {
        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDateTime past = now.minusYears(years);
        long nowSec = now.atZone(ZONE).toEpochSecond();
        long pastSec = past.atZone(ZONE).toEpochSecond();
        long sec = pastSec + (long) (rnd.nextDouble() * (nowSec - pastSec));
        return LocalDateTime.ofEpochSecond(sec, 0, ZONE.getRules().getOffset(now));
    }
}
