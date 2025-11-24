package com.boot.ict05_final_admin.domain.store.controller;

import com.boot.ict05_final_admin.domain.staffresources.service.StaffService;
import com.boot.ict05_final_admin.domain.store.dto.*;
import com.boot.ict05_final_admin.domain.store.entity.StoreStatus;
import com.boot.ict05_final_admin.domain.store.entity.StoreType;
import com.boot.ict05_final_admin.domain.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;

/**
 * 가맹점 화면(Thymeleaf) 관련 기능을 제공하는 컨트롤러입니다.
 *
 * <p>가맹점 목록 조회, 등록 화면, 상세 조회, 수정 화면 등의
 * 화면 전용 엔드포인트를 담당합니다.</p>
 */
@Controller
@RequiredArgsConstructor
@Tag(
        name = "가맹점 화면",
        description = "관리자 사이트에서 사용하는 가맹점 화면(Thymeleaf) 컨트롤러"
)
public class StoreController {

    private final StoreService storeService;
    private final StaffService staffService;

    /**
     * 가맹점 목록을 페이징 처리하여 조회한다.
     *
     * @param storeSearchDTO 가맹점 검색 조건 (이름, 상태 등)
     * @param pageable       페이징/정렬 정보 (기본: id DESC, page=1, size=10)
     * @param model          뷰에 전달할 모델 객체
     * @param request        현재 HTTP 요청(쿼리 파라미터, URL 정보 등)
     * @return 가맹점 목록 페이지 템플릿 뷰 이름
     */
    @GetMapping("/store/list")
    @Operation(
            summary = "가맹점 목록 화면",
            description = "검색 조건과 페이징 정보를 이용하여 가맹점 목록 화면을 렌더링합니다."
    )
    public String listOfficeStore(
            @Parameter(description = "가맹점 검색 조건(이름, 상태 등)")
            @ModelAttribute StoreSearchDTO storeSearchDTO,

            @Parameter(description = "페이징/정렬 정보(기본: page=1, size=10, id DESC)")
            @PageableDefault(
                    page = 1,
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,

            Model model,
            HttpServletRequest request
    ) {

        // 정렬은 id 기준 내림차순으로 고정(요구 시 동적 정렬로 확장 가능)
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber() - 1, // 1기반 -> 0기반 변환
                pageable.getPageSize(),       // 페이지 크기 유지
                Sort.by("id").descending()    // id DESC 정렬
        );

        // 상태 필터 & 페이지 필터
        boolean isFirstLoad = request.getParameter("status") == null
                && request.getParameter("s") == null
                && request.getParameter("page") == null;

        // 빈 문자열로 넘어온 status는 null로 정리
        if (storeSearchDTO.getStatus() != null &&
                storeSearchDTO.getStatus().toString().trim().isEmpty()) {
            storeSearchDTO.setStatus(null);
        }

        Page<StoreListDTO> store = storeService.selectAllOfficeStore(storeSearchDTO, pageRequest);

        model.addAttribute("store", store);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("storeSearchDTO", storeSearchDTO);

        // 헤더용 통계 데이터
        var stats = storeService.listHeaderStats();
        model.addAllAttributes(stats);

        return "store/list";
    }

    /**
     * 가맹점 등록 화면을 표시한다.
     *
     * @param model 뷰에 전달할 모델 객체
     * @return 가맹점 등록 작성 페이지 템플릿 뷰 이름
     */
    @GetMapping("/store/write")
    @Operation(
            summary = "가맹점 등록 화면",
            description = "새로운 가맹점을 등록하기 위한 화면을 렌더링합니다."
    )
    public String addOfficeStore(Model model) {

        model.addAttribute("storeWriteFormDTO", new StoreWriteFormDTO());
        model.addAttribute("StoreStatus", StoreStatus.values());
        model.addAttribute("StoreType", StoreType.values());
        model.addAttribute("ownerOptions", storeService.ownerOptions());
        model.addAttribute("hqWorkerOptions", storeService.hqWorkerOptions());

        return "store/write";
    }

    /**
     * 특정 가맹점의 상세 내용을 조회한다.
     *
     * @param id    가맹점 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 가맹점 상세 페이지 템플릿 뷰 이름
     */
    @GetMapping("store/detail/{id}")
    @Operation(
            summary = "가맹점 상세 화면",
            description = "가맹점 ID를 이용하여 상세 정보를 조회하고 화면에 렌더링합니다."
    )
    public String detailOfficeStore(
            @Parameter(description = "가맹점 ID", example = "1")
            @PathVariable Long id,
            Model model
    ) {
        StoreDetailDTO store = storeService.detailOfficeStore(id);
        model.addAttribute("store", store);
        return "store/detail";
    }

    /**
     * 특정 가맹점의 수정 화면을 표시한다.
     *
     * @param id    가맹점 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 가맹점 수정 페이지 템플릿 뷰 이름
     */
    @GetMapping("/store/modify/{id}")
    @Operation(
            summary = "가맹점 수정 화면",
            description = "기존 가맹점 정보를 수정하기 위한 화면을 렌더링합니다."
    )
    public String modifyOfficeStore(
            @Parameter(description = "가맹점 ID", example = "1")
            @PathVariable Long id,
            Model model
    ) {
        // 1) 원본 조회 (Detail DTO 또는 엔티티)
        StoreDetailDTO d = storeService.detailOfficeStore(id);

        // 2) 주소 분리 ( "주소1, 주소2" → userAddress1 / userAddress2 )
        String addr1 = null, addr2 = null;
        if (d.getStoreLocation() != null) {
            String[] parts = d.getStoreLocation().split(",", 2);
            addr1 = parts[0];
            if (parts.length > 1) addr2 = parts[1].trim();
        }

        // 3) 화면용 수정 DTO로 매핑 (필드 타입을 템플릿과 정확히 맞춤)
        StoreModifyFormDTO form = StoreModifyFormDTO.builder()
                .storeId(d.getStoreId())
                .storeName(d.getStoreName())
                .businessRegistrationNumber(d.getBusinessRegistrationNumber())
                .storePhone(d.getStorePhone())
                .storeStatus(d.getStoreStatus())     // Enum
                .storeType(d.getStoreType())         // Enum
                .storeLocation(d.getStoreLocation())
                .userAddress1(addr1)
                .userAddress2(addr2)
                .storeTotalEmployees(toInteger(d.getStoreTotalEmployees()))
                .storeContractTerm(toInteger(d.getStoreContractTerm()))
                .storeContractStartDate(toLocalDate(d.getStoreContractStartDate()))
                .storeContractAffiliateDate(toLocalDate(d.getStoreContractAffiliateDate()))
                .storeAffiliatePrice(d.getStoreAffiliatePrice())
                .storeMonthlySales(d.getStoreMonthlySales())
                .royalty(d.getRoyalty())
                .comment(d.getComment())
                .build();

        // 4) 모델 바인딩 (템플릿은 이 DTO를 기준으로 th:field 렌더)
        model.addAttribute("store", form);
        model.addAttribute("StoreStatus", StoreStatus.values());
        model.addAttribute("StoreType", StoreType.values());
        return "store/modify";
    }

    /**
     * 다양한 타입(Object) 값을 Integer로 안전하게 변환한다.
     *
     * @param v 변환 대상 값
     * @return 변환된 Integer 값, 변환 불가한 경우 null
     */
    private Integer toInteger(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return Math.toIntExact(l); // 범위 체크 포함
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.valueOf(v.toString().trim());
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 문자열 또는 다양한 타입의 값을 LocalDate로 변환한다.
     *
     * <p>허용 포맷 예시:
     * <ul>
     *     <li>yyyy-MM-dd</li>
     *     <li>yy. M. d</li>
     *     <li>yyyy. M. d</li>
     * </ul>
     * </p>
     *
     * @param v 변환 대상 값
     * @return 변환된 LocalDate, 변환 불가한 경우 null
     */
    private LocalDate toLocalDate(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDate ld) return ld;
        String s = v.toString().trim();
        if (s.endsWith(".")) s = s.substring(0, s.length() - 1).trim();

        var fmts = java.util.List.of(
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE, // yyyy-MM-dd
                java.time.format.DateTimeFormatter.ofPattern("yy. M. d"),
                java.time.format.DateTimeFormatter.ofPattern("yyyy. M. d")
        );
        for (var f : fmts) {
            try {
                return LocalDate.parse(s, f);
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    /**
     * 가맹점 수정 폼 제출을 처리한다.
     *
     * @param id  URL 경로 상의 가맹점 ID
     * @param dto 수정할 가맹점 정보 DTO
     * @return 수정 완료 후 상세 화면으로 리다이렉트
     */
    @PostMapping("/store/modify/{id}")
    @Operation(
            summary = "가맹점 수정 처리(화면용)",
            description = "화면에서 전달된 가맹점 수정 정보를 저장한 뒤, 상세 화면으로 리다이렉트합니다."
    )
    public String modifySubmit(
            @Parameter(description = "가맹점 ID", example = "1")
            @PathVariable Long id,

            @Parameter(description = "수정할 가맹점 정보 DTO")
            @ModelAttribute("store") StoreModifyFormDTO dto
    ) {
        dto.setStoreId(id);
        Long savedId = storeService.storeModify(dto).getId();
        return "redirect:/store/detail/" + savedId;
    }

}
