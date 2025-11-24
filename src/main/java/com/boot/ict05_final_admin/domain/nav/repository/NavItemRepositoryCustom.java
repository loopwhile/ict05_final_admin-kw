package com.boot.ict05_final_admin.domain.nav.repository;

import com.boot.ict05_final_admin.domain.nav.dto.NavListDTO;
import com.boot.ict05_final_admin.domain.nav.dto.NavSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NavItemRepositoryCustom {

    Page<NavListDTO> listNav(NavSearchDTO navSearchDTO, Pageable pageable);

}
