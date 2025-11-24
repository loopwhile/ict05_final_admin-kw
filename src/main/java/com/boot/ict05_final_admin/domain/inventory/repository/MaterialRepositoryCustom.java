package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 재료(Material) 커스텀 조회.
 */
public interface MaterialRepositoryCustom {

    /**
     * 검색 DTO 기반 페이지 조회.
     */
    Page<MaterialListDTO> listMaterial(MaterialSearchDTO searchDTO, Pageable pageable);

    /**
     * 검색 DTO 기반 총건수.
     */
    long countMaterial(MaterialSearchDTO searchDTO);
}
