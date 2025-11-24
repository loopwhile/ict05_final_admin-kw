package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

@SpringBootTest
class InventoryInServiceTest {

    @Autowired private InventoryInService inventoryInService;
    @Autowired private MaterialRepository materialRepository;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final int YEARS_RANGE = 2;

    private static final int MIN_PURCHASE_PRICE = 5_000;   // 입고가 하한
    private static final int MAX_PURCHASE_PRICE = 15_000;  // 입고가 상한

    private static final int MIN_INBOUND_PER_MATERIAL = 3;
    private static final int MAX_INBOUND_PER_MATERIAL = 10;

    private static final int MIN_SHELF_LIFE_DAYS = 90;
    private static final int MAX_SHELF_LIFE_DAYS = 540;

    private final Random random = new Random();

    /**
     * 1회 실행 후 @Disabled 권장.
     */
    @Test
    @Commit
    @DisplayName("최근 2년 랜덤 본사 입고 시딩 (단가 10원 반올림, 수량 단위별 범위)")
    @Disabled("1회 시딩 완료")
    void seed_randomInboundForAllMaterials() {
        List<Material> materials = materialRepository.findAll();
        if (materials.isEmpty()) {
            throw new IllegalStateException("Material이 비어 있습니다. HQ 재료 먼저 준비하세요.");
        }

        for (Material m : materials) {
            int times = randBetween(MIN_INBOUND_PER_MATERIAL, MAX_INBOUND_PER_MATERIAL);
            for (int i = 0; i < times; i++) {
                // 1) 입고일시/유통기한
                LocalDateTime inDate = randomDateTimeWithinYears(YEARS_RANGE);
                LocalDate expiration = inDate.toLocalDate().plusDays(randBetween(MIN_SHELF_LIFE_DAYS, MAX_SHELF_LIFE_DAYS));

                // 2) 가격 (입고가 정수, 판매가 = 입고가*1.1 → 10원 단위 반올림 → scale(3))
                BigDecimal unitPrice = BigDecimal.valueOf(randBetween(MIN_PURCHASE_PRICE, MAX_PURCHASE_PRICE));
                BigDecimal sellingPrice = toTenWon(unitPrice.multiply(BigDecimal.valueOf(1.1))).setScale(3, RoundingMode.HALF_UP);

                // 3) 수량 (단위별 범위)
                String unit = firstNonBlank(m.getBaseUnit(), m.getSalesUnit());
                BigDecimal quantity = randomQuantityByUnit(unit).setScale(3, RoundingMode.HALF_UP);

                // 4) DTO 구성
                InventoryInWriteDTO dto = new InventoryInWriteDTO();
                dto.setMaterialId(m.getId());
                dto.setQuantity(quantity);
                dto.setUnitPrice(unitPrice.setScale(3, RoundingMode.HALF_UP));
                dto.setSellingPrice(sellingPrice);
                dto.setInDate(inDate);
                dto.setExpirationDate(expiration);
                dto.setMemo("seed: rnd");

                // 5) 호출
                inventoryInService.insertInventoryIn(dto);
            }
        }
    }

    // ===== helpers =====
    private int randBetween(int min, int max) { return min + random.nextInt(max - min + 1); }

    private LocalDateTime randomDateTimeWithinYears(int years) {
        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDateTime past = now.minusYears(years);
        long nowSec = now.atZone(ZONE).toEpochSecond();
        long pastSec = past.atZone(ZONE).toEpochSecond();
        long sec = pastSec + (long) (random.nextDouble() * (nowSec - pastSec));
        return LocalDateTime.ofEpochSecond(sec, 0, ZONE.getRules().getOffset(now));
    }

    /** 10원 단위 반올림 */
    private BigDecimal toTenWon(BigDecimal v) {
        BigDecimal won = v.setScale(0, RoundingMode.HALF_UP);
        return won.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP).multiply(BigDecimal.TEN);
    }

    /** 단위별 수량 랜덤: g/ml 크게, ea/개 작게, 기타 중간 */
    private BigDecimal randomQuantityByUnit(String unit) {
        int min, max;
        if (unit == null) { min = 50;  max = 1_000;
        } else if (unit.equalsIgnoreCase("g") || unit.equalsIgnoreCase("ml")) { min = 500; max = 10_000;
        } else if (unit.equalsIgnoreCase("ea") || unit.equalsIgnoreCase("개")) { min = 10; max = 500;
        } else { min = 50; max = 1_000; }
        return BigDecimal.valueOf(randBetween(min, max));
    }

    private String firstNonBlank(String... s) {
        if (s == null) return null;
        for (String v : s) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
