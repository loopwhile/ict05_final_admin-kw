package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 재료(Material) JPA 리포지토리.
 *
 * <p>기본 CRUD + 코드 생성/카테고리 조회용 쿼리, 목록 커스텀 조회를 제공한다.</p>
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long>, MaterialRepositoryCustom {

    /**
     * 카테고리별 최대 코드.
     * 코드 자동 생성 시 마지막 번호 산출에 사용.
     */
    @Query("SELECT MAX(m.code) FROM Material m WHERE m.materialCategory = :category")
    String findMaxCodeByCategory(@Param("category") MaterialCategory category);

    /**
     * 카테고리별 사용중(USE) 재료 목록.
     * 본사 입고 등록 화면용.
     */
    @Query("""
           SELECT m
             FROM Material m
            WHERE m.materialCategory = :category
              AND m.materialStatus = com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus.USE
           """)
    List<Material> findByCategory(@Param("category") MaterialCategory category);
}
