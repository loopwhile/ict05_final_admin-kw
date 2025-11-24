package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 배치(LOT) JPA 리포지토리.
 *
 * <p>기본 CRUD + 화면/로직용 단순 조회만 유지.</p>
 */
@Repository
public interface InventoryBatchRepository
        extends JpaRepository<InventoryBatch, Long>, InventoryBatchRepositoryCustom {

    /** 재료별 배치 목록(입고일 내림차순) — 화면 정렬 동일 */
    List<InventoryBatch> findAllByMaterial_IdOrderByReceivedDateDesc(Long materialId);

    /** LOT 번호 중복 체크 */
    boolean existsByLotNo(String lotNo);
}
