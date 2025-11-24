package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 출고 헤더(InventoryOut) JPA 리포지토리.
 *
 * <p>현재 사용 범위: 기본 CRUD + 재료 기준 최신 출고 1건 조회.</p>
 */
@Repository
public interface InventoryOutRepository extends JpaRepository<InventoryOut, Long> {

    /**
     * 해당 재료의 가장 최근 출고 1건 조회 (출고일시 ↓, ID ↓).
     */
    Optional<InventoryOut> findTopByMaterial_IdOrderByOutDateDescIdDesc(Long materialId);
}
