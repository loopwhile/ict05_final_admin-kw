package com.boot.ict05_final_admin.domain.store.repository;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link Store} 엔티티용 Spring Data JPA 리포지토리 인터페이스.
 *
 * <p>
 * 기본적인 CRUD, 페이징, 정렬 기능은 {@link JpaRepository} 에서 상속받고,<br>
 * 복잡한 동적 쿼리 및 커스텀 로직은 {@link StoreRepositoryCustom} 에서 정의한다.
 * </p>
 */
public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {
}
