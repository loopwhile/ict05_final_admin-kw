package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialWriteFormDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialTemperature;
import com.boot.ict05_final_admin.domain.inventory.service.MaterialService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 관리자 재료 관리 컨트롤러.
 *
 * <p>재료 등록/목록/상세/수정 화면(SSR)을 제공한다.</p>
 *
 * @author 김주연
 * @since 2025.10.15
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/material")
public class MaterialController {

    private final MaterialService materialService;

    /**
     * 재료 등록 화면을 표시한다.
     *
     * @param model 뷰 모델
     * @return 재료 작성 템플릿 경로
     */
    @GetMapping("/write")
    public String addOfficeMaterial(Model model) {
        model.addAttribute("materialWriteFormDTO", new MaterialWriteFormDTO());
        model.addAttribute("MaterialCategory", MaterialCategory.values());
        model.addAttribute("MaterialTemperature", MaterialTemperature.values());
        return "material/write";
    }

    /**
     * 재료 목록을 페이징으로 조회한다.
     *
     * <p>검색 조건과 페이징 정보를 받아 서버 사이드 렌더링으로 목록을 반환한다.</p>
     *
     * @param materialSearchDTO 검색 조건(재료명/카테고리/상태 등), 옵션
     * @param pageable          페이징 정보(기본 page=1, size=10, id DESC). 1-base 페이지 인덱스를 사용한다.
     * @param model             뷰 모델
     * @param request           현재 요청(페이지네이션 링크 생성을 위해 사용)
     * @return 재료 목록 템플릿 경로
     */
    @GetMapping("/list")
    public String listMaterial(MaterialSearchDTO materialSearchDTO,
                               @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                               Model model,
                               HttpServletRequest request) {
        System.out.println("MaterialController - listMaterial()");

        // 최초 진입 여부 판단(필터/페이지 파라미터 부재)
        boolean isFirstLoad = request.getParameter("status") == null
                && request.getParameter("s") == null
                && request.getParameter("page") == null;

        // 빈 문자열로 넘어온 status 방지
        if (materialSearchDTO.getStatus() != null &&
                materialSearchDTO.getStatus().toString().trim().isEmpty()) {
            materialSearchDTO.setStatus(null);
        }

        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber() - 1,
                pageable.getPageSize(),
                Sort.by("id").descending()
        );
        Page<MaterialListDTO> materials = materialService.selectAllMaterial(materialSearchDTO, pageRequest);

        model.addAttribute("materials", materials);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("materialSearchDTO", materialSearchDTO);

        return "material/list";
    }

    /**
     * 재료 상세 화면을 표시한다.
     *
     * @param id    재료 ID
     * @param model 뷰 모델
     * @return 재료 상세 템플릿 경로
     */
    @GetMapping("/detail/{id}")
    public String detailOfficeMaterial(@PathVariable Long id, Model model) {
        Material material = materialService.detailMaterial(id);
        model.addAttribute("material", material);
        return "material/detail";
    }

    /**
     * 재료 수정 화면을 표시한다.
     *
     * @param id    재료 ID
     * @param model 뷰 모델
     * @return 재료 수정 템플릿 경로
     */
    @GetMapping("/modify/{id}")
    public String modifyOfficeMaterial(@PathVariable Long id, Model model) {
        Material material = materialService.detailMaterial(id);

        model.addAttribute("material", material);
        model.addAttribute("MaterialCategory", MaterialCategory.values());
        model.addAttribute("MaterialTemperature", MaterialTemperature.values());
        model.addAttribute("MaterialStatus", MaterialStatus.values());

        return "material/modify";
    }

    /**
     * 재료를 삭제하고 목록으로 리다이렉트한다.
     *
     * <p>주의: GET 요청으로 삭제를 수행한다. CSRF/의도치 않은 호출 방지를 위해
     * 운영 환경에서는 POST/DELETE + CSRF 토큰 사용을 권장한다.</p>
     *
     * @param id 재료 ID
     * @return 목록 페이지로 리다이렉트
     */
    @GetMapping("/delete/{id}")
    public String deleteOfficeMaterial(@PathVariable Long id, Model model) {
        materialService.deleteMaterial(id);
        return "redirect:/material/list";
    }
}
