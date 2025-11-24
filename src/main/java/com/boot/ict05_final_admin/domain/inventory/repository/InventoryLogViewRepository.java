package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 본사 입출고 통합 로그(JPA View) 리포지토리.
 *
 * <p>기본 읽기용 CRUD + 커스텀 필터 페이징을 제공한다.</p>
 */
@Repository
public interface InventoryLogViewRepository
        extends JpaRepository<InventoryLogView, Long>, InventoryLogViewRepositoryCustom {
}
