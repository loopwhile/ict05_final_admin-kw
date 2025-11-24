package com.boot.ict05_final_admin.domain.staffresources.service;

import com.boot.ict05_final_admin.domain.staffresources.dto.StaffModifyFormDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffWriteFormDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffListDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffSearchDTO;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffDepartment;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffProfile;
import com.boot.ict05_final_admin.domain.staffresources.repository.StaffRepository;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import com.boot.ict05_final_admin.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class StaffService {

    private final StaffRepository staffRepository;
    private final StoreRepository storeRepository;

    /**
     * 검색어로 필터링하여 사원 목록을 페이지 단위로 조회한다.
     *
     * @param staffSearchDTO 검색 (선택, null 가능)
     * @param pageable       페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 직원 리스트 DTO
     */
    public Page<StaffListDTO> selectAllStaff(StaffSearchDTO staffSearchDTO, Pageable pageable) {
        return staffRepository.listStaff(staffSearchDTO, pageable);
    }

    /**
     * ID를 기준으로 사원을 조회한다.
     *
     * @param id 재료 ID
     * @return 사원 엔티티, 존재하지 않으면 null
     */
    @Transactional(readOnly = true)
    public StaffProfile findById(Long id) {
        return staffRepository.findById(id).orElse(null);
    }

    /**
     * 새로운 사원을 등록한다.
     *
     * @param dto 사원 등록 정보
     * @return 저장된 사원 ID
     */
    public long insertOfficeStaff(StaffWriteFormDTO dto) {

        String address = "";
        String address1 = dto.getUserAddress1();
        String address2 = dto.getUserAddress2();
        address = address1 + "," + address2;
        if(address.equals(",")) {
            address = "";
        }
        dto.setStaffAddress(address);

        Store store = null;
        if (dto.getStoreIdFk() != null) {
            store = storeRepository.findById(dto.getStoreIdFk())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 근무지입니다."));
        }

        StaffProfile staff = StaffProfile.builder()
                .store(store)
                .staffName(dto.getStaffName())
                .staffEmploymentType(dto.getStaffEmploymentType())
                .staffDepartment(dto.getStaffDepartment())
                .staffEmail(dto.getStaffEmail())
                .staffPhone(dto.getStaffPhone())
                .staffAddress(dto.getStaffAddress())
                .staffBirth(dto.getStaffBirth())
                .staffStartDate(dto.getStaffStartDate())
                .staffEndDate(dto.getStaffEndDate())
                .build();

        StaffProfile saved = staffRepository.save(staff);
        Long id = saved.getId();

        return id;
    }

    /**
     * 사원 상세 정보를 조회한다.
     *
     * @param id 사원 ID
     * @return 사원 엔티티, 존재하지 않으면 null
     */
    @Transactional(readOnly = true)
    public StaffProfile detailStaff(Long id) {
        var raw = staffRepository.findRaw(id).isPresent();
        var left = staffRepository.findWithStoreByIdLeft(id).isPresent();
        var plain = staffRepository.findById(id).isPresent();
        log.warn("[STAFF-DEBUG] id={}, raw={}, leftFetch={}, plain={}", id, raw, left, plain);

        return staffRepository.findWithStoreByIdLeft(id)
                .or(() -> staffRepository.findById(id)) // 연관 무시하고 본체만이라도
                .orElseThrow(() -> new IllegalArgumentException("해당 사원이 존재하지 않습니다."));
    }

    /**
     * 기존 사원 정보를 수정한다.
     *
     * @param dto 수정할 데이터
     * @return 수정된 재료 엔티티
     */
    public StaffProfile staffModify(StaffModifyFormDTO dto) {

        String address = "";
        String address1 = dto.getUserAddress1();
        String address2 = dto.getUserAddress2();
        address = address1 + "," + address2;
        if(address.equals(",")) {
            address = "";
        }
        dto.setStaffAddress(address);

        StaffProfile staffProfile = findById(dto.getId());
        if (staffProfile == null) throw new IllegalArgumentException("해당 사원이 존재하지 않습니다.");

        staffProfile.updateStaff(dto);

        // 2) 근무지 반영
        if (dto.getStoreIdFk() != null) {
            Store store = storeRepository.findById(dto.getStoreIdFk())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 근무지입니다."));
            staffProfile.changeStore(store);  // ← 아래 4번 참고
        } else {
            staffProfile.changeStore(null);
        }

        return staffProfile;
    }

    /**
     * 사원 ID를 받아 삭제한다.
     *
     * @param id 사원 ID
     */
    public void deleteStaff(Long id) {
        staffRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listHeaderStats() {
        long total = staffRepository.countAll();
        long active = staffRepository.countActive();
        long office = staffRepository.countByDepartment(StaffDepartment.OFFICE);
        double avgYr = staffRepository.avgTenureYears(LocalDateTime.now());

        return Map.of(
                "totalStaff", total,
                "activeStaff", active,
                "officeStaff", office,
                "avgTenureYears", avgYr
        );
    }
}

