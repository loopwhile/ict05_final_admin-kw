package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryIn;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryInRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 본사 입고 서비스.
 *
 * <p>
 * 본사 재고 입고 처리, 현재고 갱신, 배치(LOT) 생성까지 단일 트랜잭션으로 수행한다.
 * 수량/단가는 소수점 3자리 HALF_UP으로 정규화한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class InventoryInService {

    private final EntityManager em;
    private final InventoryInRepository inRepo;
    private final InventoryBatchRepository batchRepo;
    private final InventoryStockService stockService; // 재고 증감/상태 갱신 전담
    private final UnitPriceService unitPriceService;

    /** 소수점 3자리 HALF_UP 고정 */
    private static BigDecimal s3(final BigDecimal v) {
        return v == null ? null : v.setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * 본사 입고 등록.
     *
     * <p>절차</p>
     * <ol>
     *   <li>파라미터 검증 및 정규화</li>
     *   <li>단가 이력 기록(매입/판매)</li>
     *   <li>본사 재고 += qty (비관잠금은 {@link InventoryStockService} 내부에서 수행)</li>
     *   <li>입고 로그 저장(헤더: {@code stockAfter=after})</li>
     *   <li>동일 {@code lotNo}로 배치(InventoryBatch) 생성</li>
     * </ol>
     *
     * @param dto 입고 등록 DTO
     * @return 생성된 입고 ID
     * @throws IllegalArgumentException 필수값 누락 또는 유효성 위반 시
     */
    @Transactional
    public Long insertInventoryIn(final InventoryInWriteDTO dto) {
        // 0) 검증
        if (dto.getMaterialId() == null) {
            throw new IllegalArgumentException("materialId is required");
        }
        if (dto.getQuantity() == null || dto.getQuantity().signum() <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        if (dto.getUnitPrice() == null || dto.getUnitPrice().signum() < 0) {
            throw new IllegalArgumentException("unitPrice must be >= 0");
        }

        // 1) 정규화
        final Material materialRef = em.getReference(Material.class, dto.getMaterialId());
        final BigDecimal qty = s3(dto.getQuantity());
        final BigDecimal unitPrice = s3(dto.getUnitPrice());
        final BigDecimal sellingPrice = s3(dto.getSellingPrice());
        final LocalDateTime inDate = dto.getInDate() != null ? dto.getInDate() : LocalDateTime.now();

        // 2) 단가 이력 추가(정책: 입고 시점 기준)
        unitPriceService.addPricesForMaterial(materialRef.getId(), unitPrice, sellingPrice);

        // 3) 재고 갱신: 본사 재고 += qty → after 반환
        final BigDecimal after = stockService.addToInventory(materialRef.getId(), qty); // 내부에서 touchAfterQuantityChange() 호출

        // 4) lotNo 생성
        final String lotNo = generateLotNo();

        // 5) 입고 로그 저장(헤더)
        InventoryIn in = InventoryIn.builder()
                .material(materialRef)
                .inDate(inDate)
                .quantity(qty)
                .unitPrice(unitPrice)
                .sellingPrice(sellingPrice)
                .memo(dto.getMemo())
                .lotNo(lotNo)
                .stockAfter(after) // 필수: 이후 화면/로그 일관성
                .build();
        in = inRepo.save(in);

        // 6) 배치 저장(LOT)
        InventoryBatch batch = InventoryBatch.builder()
                .material(materialRef)
                .store(null) // 본사 배치
                .receivedDate(inDate)
                .expirationDate(dto.getExpirationDate())
                .receivedQuantity(qty)
                .quantity(qty) // 초기 잔량 = 입고수량
                .unitPrice(unitPrice)
                .lotNo(lotNo)
                .build();
        batchRepo.save(batch);

        return in.getId();
    }

    /**
     * 입고 삭제.
     *
     * <p>
     * 단순 헤더 삭제만 수행한다. 배치/재고 롤백은 별도 정책에 따른다.
     * </p>
     *
     * @param inId 입고 ID
     */
    @Transactional
    public void deleteIn(final Long inId) {
        inRepo.deleteById(inId);
    }

    /**
     * LOT 번호 생성기.
     *
     * <p>포맷: {@code LOT-YYMMDD-######}. 충돌 시 최대 5회 재시도 후 타임베이스 시퀀스로 폴백.</p>
     */
    private String generateLotNo() {
        final LocalDate today = LocalDate.now();
        final String ymd = String.format("%02d%02d%02d",
                today.getYear() % 100, today.getMonthValue(), today.getDayOfMonth());

        for (int i = 0; i < 5; i++) {
            String seq = String.format("%06d", Math.floorMod(System.nanoTime(), 1_000_000));
            String lot = "LOT-" + ymd + "-" + seq;
            if (!inRepo.existsByLotNo(lot) && !batchRepo.existsByLotNo(lot)) {
                return lot;
            }
        }
        return "LOT-" + ymd + "-" + String.format("%06d", Math.floorMod(System.currentTimeMillis(), 1_000_000));
    }
}
