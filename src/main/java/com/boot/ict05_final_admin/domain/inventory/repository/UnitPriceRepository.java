package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.UnitPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 단가 이력(JPA) 리포지토리.
 *
 * <p>조회 확장은 {@link UnitPriceRepositoryCustom} 사용.</p>
 */
@Repository
public interface UnitPriceRepository
        extends JpaRepository<UnitPrice, Long>, UnitPriceRepositoryCustom {
}
