package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 본사 재고(Inventory) JPA 리포지토리.
 *
 * <p>기본 CRUD + 비관잠금 조회/보조 조회 제공.</p>
 */
@Repository
public interface InventoryRepository
        extends JpaRepository<Inventory, Long>, InventoryRepositoryCustom {

    /**
     * 재료 ID 기준 비관잠금(PESSIMISTIC_WRITE) 조회.
     * 수량 증감 트랜잭션의 경합 방지 용도.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.material.id = :materialId")
    Optional<Inventory> findByMaterialIdForUpdate(@Param("materialId") Long materialId);

    /**
     * 재료 존재 여부 확인.
     */
    boolean existsByMaterial_Id(Long materialId);

    /**
     * 재료 ID 기준 단건 조회.
     */
    Optional<Inventory> findByMaterialId(Long materialId);
}
