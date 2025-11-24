package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 재고 조정(InventoryAdjustment) JPA 리포지토리.
 *
 * <p>기본 CRUD + 커스텀 쿼리(기간/합계/최근시각)를 제공한다.</p>
 */
@Repository
public interface InventoryAdjustmentRepository
        extends JpaRepository<InventoryAdjustment, Long>, InventoryAdjustmentRepositoryCustom {
}
