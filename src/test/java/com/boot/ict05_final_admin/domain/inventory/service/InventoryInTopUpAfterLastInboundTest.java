package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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

/**
 * HQ 재고 Top-up 테스트 (네이티브 쿼리 버전)
 *
 * - 기준: 재료별 가장 최근 inventory_in 1건 + lot로 inventory_batch 조인
 * - 신규 입고일: 최근 입고일 + 7일
 * - 수량/단가/판매가: 최근 값 복제 (판매가 null이면 단가*1.1 후 10원 반올림, scale(3))
 * - 유통기한: 최근 입고 in_date → batch.expiration_date 간격 유지, 없으면 +180일
 *
 * 사용 컬럼 (네이티브):
 *  inventory_in:
 *    inventory_in_date (datetime)
 *    inventory_in_quantity (decimal(15,3))
 *    inventory_in_unit_price (bigint)
 *    inventory_in_selling_price (bigint, nullable)
 *    inventory_lot (varchar, UNIQUE)
 *    material_id_fk (bigint)
 *
 *  inventory_batch:
 *    inventory_batch_lot_no (varchar, UNIQUE)
 *    inventory_batch_expiration_date (date, nullable)
 */
@SpringBootTest
class InventoryInTopUpAfterLastInboundTest {

    @Autowired private InventoryInService inventoryInService;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private EntityManager em;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Test
    @Commit
    @DisplayName("본사 입고 Top-up: 최근 입고 이후(+7일)로 재입고 1회 생성 (네이티브 조인)")
    @Disabled("1회 시딩 완료")
    void topUpOnceAfterLastInboundPerMaterial() {
        final List<Material> materials = materialRepository.findAll();
        if (materials.isEmpty()) {
            throw new IllegalStateException("Material 데이터가 없습니다. HQ 재료를 먼저 준비하세요.");
        }

        for (Material m : materials) {
            LastInbound last = findLastInboundNative(m.getId());
            if (last == null) {
                // 기존 입고가 없는 재료는 스킵
                continue;
            }

            // 신규 입고일: 최근 입고일 + 7일
            LocalDateTime newInDate = last.inDate.plusDays(7);

            // 유통기한: in_date→expiration 간격 유지, 없으면 +180일
            LocalDate newExpiration = computeNextExpiration(last.inDate, last.expiration, newInDate);

            // 판매가: null이면 단가*1.1을 10원 단위 반올림
            BigDecimal sellingPrice = (last.sellingPrice != null)
                    ? last.sellingPrice
                    : toTenWon(last.unitPrice.multiply(BigDecimal.valueOf(1.1))).setScale(3, RoundingMode.HALF_UP);

            InventoryInWriteDTO dto = new InventoryInWriteDTO();
            dto.setMaterialId(m.getId());
            dto.setQuantity(scale3(last.quantity));
            dto.setUnitPrice(scale3(last.unitPrice));
            dto.setSellingPrice(scale3(sellingPrice));
            dto.setInDate(newInDate);
            dto.setExpirationDate(newExpiration);
            dto.setMemo("seed: topup after last inbound (lot:" + last.lot + ")");

            inventoryInService.insertInventoryIn(dto);
        }
    }

    /**
     * 네이티브 쿼리: 재료별 가장 최근 입고 1건 + LOT로 배치 조인
     * LIMIT 1로 최근 1건만.
     */
    private LastInbound findLastInboundNative(Long materialId) {
        String sql =
                "SELECT " +
                        "  i.inventory_in_date, " +                 // 0: datetime
                        "  b.inventory_batch_expiration_date, " +   // 1: date (nullable)
                        "  i.inventory_in_quantity, " +             // 2: decimal(15,3)
                        "  i.inventory_in_unit_price, " +           // 3: bigint
                        "  i.inventory_in_selling_price, " +        // 4: bigint (nullable)
                        "  i.inventory_lot " +                      // 5: varchar
                        "FROM inventory_in i " +
                        "LEFT JOIN inventory_batch b " +
                        "  ON b.inventory_batch_lot_no = i.inventory_lot " +
                        "WHERE i.material_id_fk = :mid " +
                        "ORDER BY i.inventory_in_date DESC " +
                        "LIMIT 1";

        Query q = em.createNativeQuery(sql);
        q.setParameter("mid", materialId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return null;

        Object[] r = rows.get(0);

        // 0: datetime → LocalDateTime
        LocalDateTime inDate = toLocalDateTime(r[0]);

        // 1: date → LocalDate (nullable)
        LocalDate expiration = toLocalDate(r[1]);

        // 2: decimal → BigDecimal
        BigDecimal quantity = toBigDecimal(r[2]);

        // 3: bigint → BigDecimal
        BigDecimal unitPrice = toBigDecimal(r[3]);

        // 4: bigint → BigDecimal (nullable)
        BigDecimal sellingPrice = toBigDecimal(r[4]);

        // 5: varchar → String
        String lot = (r[5] == null) ? null : r[5].toString();

        return new LastInbound(inDate, expiration, quantity, unitPrice, sellingPrice, lot);
    }

    private LocalDate computeNextExpiration(LocalDateTime lastIn, LocalDate lastExp, LocalDateTime newIn) {
        if (lastIn != null && lastExp != null) {
            long gapDays = java.time.Duration.between(
                    lastIn.atZone(ZONE).toInstant(),
                    lastExp.atStartOfDay(ZONE).toInstant()
            ).toDays();
            if (gapDays > 0) {
                return newIn.toLocalDate().plusDays(gapDays);
            }
        }
        return newIn.toLocalDate().plusDays(180);
    }

    /** 10원 단위 반올림 */
    private BigDecimal toTenWon(BigDecimal v) {
        BigDecimal won = v.setScale(0, RoundingMode.HALF_UP);
        return won.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP).multiply(BigDecimal.TEN);
    }

    private BigDecimal scale3(BigDecimal v) {
        return v == null ? null : v.setScale(3, RoundingMode.HALF_UP);
    }

    /** Number/SQL 타입들을 안전하게 BigDecimal로 변환 */
    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        // 드라이버가 byte[], String 등으로 줄 수도 있어 방어
        return new BigDecimal(o.toString());
    }

    /** datetime → LocalDateTime */
    private LocalDateTime toLocalDateTime(Object o) {
        if (o == null) return null;
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (o instanceof java.time.LocalDateTime ldt) return ldt;
        // 드라이버가 String으로 주는 경우
        return LocalDateTime.parse(o.toString().replace(' ', 'T'));
    }

    /** date → LocalDate */
    private LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof java.time.LocalDate ld) return ld;
        return LocalDate.parse(o.toString());
    }

    private record LastInbound(
            LocalDateTime inDate,
            LocalDate expiration,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal sellingPrice,
            String lot
    ) {}
}
