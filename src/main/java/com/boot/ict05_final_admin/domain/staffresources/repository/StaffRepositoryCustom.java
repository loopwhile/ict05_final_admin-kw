package com.boot.ict05_final_admin.domain.staffresources.repository;

import com.boot.ict05_final_admin.domain.staffresources.dto.StaffListDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StaffRepositoryCustom {

    Page<StaffListDTO> listStaff(StaffSearchDTO staffSearchDTO, Pageable pageable);

    long countStaff(StaffSearchDTO staffSearchDTO);

    long countAll();

    long countActive();

    long countByDepartment(com.boot.ict05_final_admin.domain.staffresources.entity.StaffDepartment dept);

    double avgTenureYears(java.time.LocalDateTime now);

}
