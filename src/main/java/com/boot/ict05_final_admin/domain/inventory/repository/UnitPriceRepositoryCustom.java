package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.UnitPrice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 단가 이력 커스텀 조회.
 */
public interface UnitPriceRepositoryCustom {

    /** 기준 시각(at)에 유효한 최신 매입단가 */
    Optional<UnitPrice> findLatestPurchasePrice(Long materialId, LocalDateTime at);

    /** 매입단가 이력 최근 N건 */
    List<UnitPrice> historyPurchasePrice(Long materialId, int limit);

    /** 기준 시각(at)에 유효한 최신 출고단가 */
    Optional<UnitPrice> findLatestSellingPrice(Long materialId, LocalDateTime at);

    /** 출고단가 이력 최근 N건 */
    List<UnitPrice> historySellingPrice(Long materialId, int limit);
}
