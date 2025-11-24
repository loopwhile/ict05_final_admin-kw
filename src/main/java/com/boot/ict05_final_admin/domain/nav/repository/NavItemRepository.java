package com.boot.ict05_final_admin.domain.nav.repository;

import com.boot.ict05_final_admin.domain.nav.entity.NavItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NavItemRepository extends JpaRepository<NavItem, Long>, NavItemRepositoryCustom {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NavItem n set n.navItemEnabled = :enabled where n.id = :id")
    int updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);

}
