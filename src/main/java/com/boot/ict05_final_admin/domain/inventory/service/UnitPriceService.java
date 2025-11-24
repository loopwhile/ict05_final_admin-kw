package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.UnitPrice;
import com.boot.ict05_final_admin.domain.inventory.entity.UnitPriceType;
import com.boot.ict05_final_admin.domain.inventory.repository.UnitPriceRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 단가 서비스.
 *
 * <p>재료별 매입/판매 단가의 등록·수정·조회 이력을 관리한다.</p>
 */
@Service
@RequiredArgsConstructor
public class UnitPriceService {

    private final UnitPriceRepository unitPriceRepository;
    private final EntityManager em;

    /**
     * 매입가 신규 등록.
     *
     * <p>validFrom이 null이면 현재 시각으로 대체한다. 가격 파라미터가 null이면 0으로 대체한다.</p>
     *
     * @param materialId 재료 ID
     * @param price      매입 단가(null 허용 → 0)
     * @param validFrom  적용 시작일시(null 허용 → now)
     * @param validTo    적용 종료일시(선택)
     * @return 생성된 단가 ID
     */
    @Transactional
    public Long setPurchasePrice(Long materialId,
                                 BigDecimal price,
                                 LocalDateTime validFrom,
                                 LocalDateTime validTo) {

        UnitPrice up = UnitPrice.builder()
                .material(em.getReference(Material.class, materialId))
                .type(UnitPriceType.PURCHASE)
                .purchasePrice(price != null ? price : BigDecimal.ZERO)
                .sellingPrice(BigDecimal.ZERO)
                .validFrom(validFrom != null ? validFrom : LocalDateTime.now())
                .validTo(validTo)
                .build();

        unitPriceRepository.save(up);
        return up.getId();
    }

    /**
     * 매입가 수정.
     *
     * <p>기존 레코드의 {@code purchasePrice}만 갱신한다.
     * 유효구간 분리(종료 후 새 행 생성) 정책은 별도 메서드로 다룬다.</p>
     *
     * @param unitPriceId 단가 ID
     * @param price       수정 단가(null 허용 → 0)
     * @return 수정된 단가 ID
     */
    @Transactional
    public Long updatePurchasePrice(Long unitPriceId, BigDecimal price) {
        UnitPrice row = unitPriceRepository.findById(unitPriceId)
                .orElseThrow(() -> new IllegalArgumentException("단가를 찾을 수 없습니다. id=" + unitPriceId));
        row.setPurchasePrice(price != null ? price : BigDecimal.ZERO); // dirty checking
        return row.getId();
    }

    /**
     * 기준 시점의 최신 매입가 조회.
     *
     * @param materialId 재료 ID
     * @param at         기준 시각(null 허용 → now)
     * @return 기준 시점 유효 매입가(Optional)
     */
    @Transactional(readOnly = true)
    public Optional<UnitPrice> latestPurchasePrice(Long materialId, LocalDateTime at) {
        return unitPriceRepository.findLatestPurchasePrice(
                materialId, at != null ? at : LocalDateTime.now());
    }

    /**
     * 재료의 매입 단가와 판매 단가를 각각 새 레코드로 추가.
     *
     * <p>두 행을 같은 시각(now) 기준으로 생성한다. null 가격은 0으로 대체.</p>
     *
     * @param materialId   재료 ID
     * @param unitPrice    매입 단가
     * @param sellingPrice 판매 단가
     */
    @Transactional
    public void addPricesForMaterial(Long materialId, BigDecimal unitPrice, BigDecimal sellingPrice) {
        LocalDateTime now = LocalDateTime.now();

        // 매입 단가 추가
        UnitPrice purchasePrice = UnitPrice.builder()
                .material(em.getReference(Material.class, materialId))
                .type(UnitPriceType.PURCHASE)
                .purchasePrice(unitPrice != null ? unitPrice : BigDecimal.ZERO)
                .sellingPrice(BigDecimal.ZERO)
                .validFrom(now)
                .build();
        unitPriceRepository.save(purchasePrice);

        // 판매 단가 추가
        UnitPrice sellPrice = UnitPrice.builder()
                .material(em.getReference(Material.class, materialId))
                .type(UnitPriceType.SELLING)
                .purchasePrice(BigDecimal.ZERO)
                .sellingPrice(sellingPrice != null ? sellingPrice : BigDecimal.ZERO)
                .validFrom(now)
                .build();
        unitPriceRepository.save(sellPrice);
    }
}
