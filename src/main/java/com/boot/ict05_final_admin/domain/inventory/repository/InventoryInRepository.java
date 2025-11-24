package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 입고 헤더(InventoryIn) JPA 리포지토리.
 *
 * <p>현재 서비스 사용 범위: 기본 CRUD + LOT 중복 체크.</p>
 */
@Repository
public interface InventoryInRepository extends JpaRepository<InventoryIn, Long> {

    /**
     * LOT 번호 중복 체크.
     */
    boolean existsByLotNo(String lotNo);
}
