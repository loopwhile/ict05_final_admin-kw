package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 출고-LOT JPA 리포지토리.
 *
 * <p>기본 CRUD + 서비스에서 사용하는 커스텀(출고ID 기준 조회)만 유지.</p>
 */
@Repository
public interface InventoryOutLotRepository
        extends JpaRepository<InventoryOutLot, Long>, InventoryOutLotRepositoryCustom {
}
