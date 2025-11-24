package com.boot.ict05_final_admin.domain.menu.repository;

import com.boot.ict05_final_admin.domain.menu.entity.Menu;
import com.boot.ict05_final_admin.domain.menu.entity.StoreMenu;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreMenuRepository extends JpaRepository<StoreMenu, Long> {

    boolean existsByStoreAndMenu(Store store, Menu menu);

}

