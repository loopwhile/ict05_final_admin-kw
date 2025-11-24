package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.StoreMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 가맹점 재료 JPA 리포지토리.
 */
@Repository
public interface StoreMaterialRepository
        extends JpaRepository<StoreMaterial, Long>, StoreMaterialRepositoryCustom {
}
