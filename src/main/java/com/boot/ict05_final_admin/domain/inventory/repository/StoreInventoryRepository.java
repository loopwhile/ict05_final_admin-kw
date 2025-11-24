package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.StoreInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 가맹점 재고 JPA 리포지토리.
 */
@Repository
public interface StoreInventoryRepository
        extends JpaRepository<StoreInventory, Long>, StoreInventoryRepositoryCustom {

    /** 가맹점 재료 ID로 단건 조회 */
    @Query("SELECT si FROM StoreInventory si WHERE si.storeMaterial.id = :storeMaterialId")
    Optional<StoreInventory> findByStoreMaterialId(@Param("storeMaterialId") Long storeMaterialId);

    /** (HQ 재료 기준) 가맹점+본사재료로 비관잠금 조회 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select si
             from StoreInventory si
             join si.storeMaterial sm
             join sm.material m
            where si.store.id = :storeId
              and sm.isHqMaterial = true
              and m.id = :materialId
           """)
    Optional<StoreInventory> findByStoreIdAndMaterialIdForUpdate(@Param("storeId") Long storeId,
                                                                 @Param("materialId") Long materialId);

    /** (HQ 재료 기준) 가맹점+본사재료로 조회 */
    @Query("""
           select si
             from StoreInventory si
             join si.storeMaterial sm
             join sm.material m
            where si.store.id = :storeId
              and sm.isHqMaterial = true
              and m.id = :materialId
           """)
    Optional<StoreInventory> findByStoreIdAndMaterialId(@Param("storeId") Long storeId,
                                                        @Param("materialId") Long materialId);

    /** (HQ 재료 기준) 존재 여부 */
    @Query("""
           select case when count(si)>0 then true else false end
             from StoreInventory si
             join si.storeMaterial sm
             join sm.material m
            where si.store.id = :storeId
              and sm.isHqMaterial = true
              and m.id = :materialId
           """)
    boolean existsByStoreIdAndMaterialId(@Param("storeId") Long storeId,
                                         @Param("materialId") Long materialId);

    /** (HQ 재료 기준) 가맹점 전체 또는 특정 본사재료 페이징 */
    @Query("""
           select si
             from StoreInventory si
             join si.storeMaterial sm
             join sm.material m
            where si.store.id = :storeId
              and sm.isHqMaterial = true
              and (:materialId is null or m.id = :materialId)
           """)
    Page<StoreInventory> findAllByStoreIdAndMaterialId(@Param("storeId") Long storeId,
                                                       @Param("materialId") Long materialId,
                                                       Pageable pageable);
}
