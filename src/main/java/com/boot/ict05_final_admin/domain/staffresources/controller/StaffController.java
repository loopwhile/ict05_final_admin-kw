package com.boot.ict05_final_admin.domain.staffresources.controller;

import com.boot.ict05_final_admin.domain.staffresources.dto.StaffWriteFormDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffListDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffSearchDTO;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffDepartment;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffEmploymentType;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffProfile;
import com.boot.ict05_final_admin.domain.staffresources.service.StaffService;
import com.boot.ict05_final_admin.domain.store.dto.FindStoreDTO;
import com.boot.ict05_final_admin.domain.store.service.StoreService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * 직원 관리 화면 컨트롤러.
 *
 * 직원 목록 조회, 등록 화면, 상세 조회, 수정 화면, 삭제 등
 * 화면 렌더링과 모델 구성 역할을 담당한다.
 */
@Controller
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;
    private final StoreService storeService;

    /**
     * 직원 목록을 페이징 처리하여 조회한다.
     *
     * @param staffSearchDTO   (선택) 작성자 이름으로 검색할 경우 전달되는 값
     * @param pageable 페이지 번호, 크기, 정렬 조건을 포함한 페이징 객체
     * @param model    뷰에 전달할 모델 객체
     * @return 직원 목록 페이지 뷰 이름
     */
    @GetMapping("/staff/list")
    public String listOfficeStaff(StaffSearchDTO staffSearchDTO,
                            @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC)Pageable pageable,
                            Model model,
                            HttpServletRequest request) {
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber()-1,
                pageable.getPageSize(),
                Sort.by("id").descending());

        Page<StaffListDTO> staffs = staffService.selectAllStaff(staffSearchDTO, pageRequest);

        model.addAttribute("staffs", staffs);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("staffSearchDTO", staffSearchDTO);

        var stats = staffService.listHeaderStats();
        model.addAttribute("totalStaff",      stats.get("totalStaff"));
        model.addAttribute("activeStaff",     stats.get("activeStaff"));
        model.addAttribute("officeStaff",     stats.get("officeStaff"));
        model.addAttribute("avgTenureYears",  stats.get("avgTenureYears"));

        return "staff/list";
    }

    /**
     * 사원 등록 화면을 표시한다.
     *
     * @param model 뷰에 전달할 모델 객체
     * @return 사원등록 작성 페이지 뷰 이름
     */
    @GetMapping("/staff/write")
    public String addOfficeStaff(Model model) {

        List<FindStoreDTO> stores = storeService.findStoreName();

        model.addAttribute("staffWriteFormDTO", new StaffWriteFormDTO());
        model.addAttribute("StaffDepartment", StaffDepartment.values());
        model.addAttribute("StaffEmploymentType", StaffEmploymentType.values());
        model.addAttribute("stores", stores);

        return "staff/write";
    }

    /**
     * 특정 사원의 상세 내용을 조회한다.
     *
     * @param id    사원 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 사원 상세 페이지 뷰 이름
     */
    @GetMapping("staff/detail/{id}")
    public String detailOfficeStaff(@PathVariable Long id, Model model) {
        StaffProfile staffProfile = staffService.detailStaff(id);

        model.addAttribute("staff", staffProfile);

        return "staff/detail";
    }

    /**
     * 특정 사원의 수정 화면을 표시한다.
     *
     * @param id    재료 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 사원 수정 페이지 뷰 이름
     */
    @GetMapping("/staff/modify/{id}")
    public String modifyOfficeStaff(@PathVariable Long id, Model model) {

        StaffProfile staffProfile = staffService.detailStaff(id);
        List<FindStoreDTO> stores = storeService.findStoreName();

        model.addAttribute("staff", staffProfile);
        model.addAttribute("StaffDepartment", StaffDepartment.values());
        model.addAttribute("StaffEmploymentType", StaffEmploymentType.values());
        model.addAttribute("stores", stores);

        return "staff/modify";
    }

    @GetMapping("/staff/delete/{id}")
    public String deleteOfficeStaff(@PathVariable Long id, Model model) {
        staffService.deleteStaff(id);
        return "redirect:/staff/list";
    }
}
