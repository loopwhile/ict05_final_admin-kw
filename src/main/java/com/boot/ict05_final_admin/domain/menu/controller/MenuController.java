package com.boot.ict05_final_admin.domain.menu.controller;

import com.boot.ict05_final_admin.config.ProjectAttribute;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import com.boot.ict05_final_admin.domain.menu.dto.*;
import com.boot.ict05_final_admin.domain.menu.entity.Menu;
import com.boot.ict05_final_admin.domain.menu.entity.MenuCategory;
import com.boot.ict05_final_admin.domain.menu.entity.MenuShow;
import com.boot.ict05_final_admin.domain.menu.entity.RecipeUnit;
import com.boot.ict05_final_admin.domain.menu.repository.MenuCategoryRepository;
import com.boot.ict05_final_admin.domain.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 본사 메뉴 관리 컨트롤러.
 *
 * <p>
 * 메뉴 작성, 목록 조회, 상세 조회, 수정 화면을 제공하며
 * 카테고리/재료 옵션을 함께 구성한다.
 * </p>
 */
@Controller
@RequiredArgsConstructor
@Tag(name = "Admin · Menu", description = "본사 메뉴 관리 화면 컨트롤러")
public class MenuController {

    private final MenuService menuService;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MaterialRepository materialRepository;

    /**
     * 메뉴 목록 화면을 페이징/정렬/검색 조건과 함께 렌더링한다.
     *
     * @param menuSearchDTO 검색/필터 파라미터
     * @param pageable      페이지 정보
     * @param model         뷰 모델
     * @param request       현재 요청(쿼리스트링 유지 등)
     * @return 목록 뷰 이름
     */
    @Operation(
            summary = "메뉴 목록 화면",
            description = "검색/필터/페이징 조건으로 메뉴 목록을 조회하여 목록 화면을 렌더링한다."
    )
    @GetMapping("/menu/list")
    public String listStoreMenu(
            @Parameter(description = "검색/필터 파라미터") MenuSearchDTO menuSearchDTO,
            @PageableDefault(page = 1, size = 10, sort = "menuId", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable,
            Model model,
            HttpServletRequest request) {

        int size = resolveSize(menuSearchDTO.getSize(), pageable.getPageSize());
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "menuId");

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, size, sort);

        Page<MenuListDTO> menus = menuService.selectAllStoreMenu(menuSearchDTO, pageRequest);

        // 리프(레벨3) + '세트메뉴' 우선 추가
        List<MenuCategory> categories = new ArrayList<>(
                menuCategoryRepository.findAllByMenuCategoryLevel(3, Sort.by("menuCategoryName").ascending())
        );
        List<MenuCategory> finalCategories = categories;
        menuCategoryRepository.findByMenuCategoryName("세트메뉴")
                .ifPresent(c -> finalCategories.add(0, c));

        // 중복 제거
        categories = categories.stream()
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toMap(
                                MenuCategory::getMenuCategoryId,
                                c -> c,
                                (a, b) -> a,
                                LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        model.addAttribute("menus", menus);
        model.addAttribute("menuSearchDTO", menuSearchDTO);
        model.addAttribute("menuCategories", categories);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));

        return "menu/list";
    }

    /** 페이지 크기 파싱 유틸. 유효하지 않으면 기본값을 사용한다. */
    private int resolveSize(String s, int fallback) {
        try {
            if (s == null || s.isBlank()) return fallback;
            int v = Integer.parseInt(s);
            return (v < 1) ? fallback : v;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * 메뉴 작성 화면을 렌더링한다.
     *
     * @param model 뷰 모델
     * @return 작성 뷰 이름
     */
    @Operation(summary = "메뉴 작성 화면", description = "메뉴 작성 폼과 카테고리/재료 옵션을 구성하여 렌더링한다.")
    @GetMapping("/menu/write")
    public String writeForm(Model model) {

        MenuWriteFormDTO form = new MenuWriteFormDTO();
        form.setMenuShow(MenuShow.SHOW);

        model.addAttribute("menuWriteFormDTO", form);
        model.addAttribute("menuShowValues", MenuShow.values());
        model.addAttribute("units", RecipeUnit.values());

        List<MenuCategory> categories = menuCategoryRepository.findSetAndLevel3Categories();
        model.addAttribute("menuCategories", categories);

        // 재료 옵션
        List<Material> mainEntities  = materialRepository.findByCategory(MaterialCategory.BASE);
        List<Material> sauceEntities = materialRepository.findByCategory(MaterialCategory.SAUCE);

        List<MaterialSimpleDTO> mainOptions = mainEntities.stream()
                .map(m -> new MaterialSimpleDTO(m.getId(), m.getName()))
                .toList();
        List<MaterialSimpleDTO> sauceOptions = sauceEntities.stream()
                .map(m -> new MaterialSimpleDTO(m.getId(), m.getName()))
                .toList();

        model.addAttribute("mainOptions", mainOptions);
        model.addAttribute("sauceOptions", sauceOptions);

        return "menu/write";
    }

    /**
     * 메뉴 작성 폼을 제출하여 저장한다.
     *
     * @param dto            작성 폼 DTO
     * @param bindingResult  검증 결과
     * @param ra             리다이렉트 속성
     * @return 상세 화면으로 리다이렉트
     */
    @Operation(
            summary = "메뉴 저장",
            description = "작성 폼을 검증 후 저장하고 상세 화면으로 이동한다."
    )
    @PostMapping("/menu/write")
    public String submitMenuWrite(
            @Validated @ModelAttribute("menuWriteFormDTO") MenuWriteFormDTO dto,
            BindingResult bindingResult,
            RedirectAttributes ra) {

        if (dto.getMainMaterials() == null) dto.setMainMaterials(new ArrayList<>());
        if (dto.getSauceMaterials() == null) dto.setSauceMaterials(new ArrayList<>());

        if (bindingResult.hasErrors()) {
            return "menu/write";
        }

        Long menuId = menuService.insertStoreMenu(dto);
        ra.addFlashAttribute("message", "메뉴가 저장되었습니다.");
        return "redirect:/menu/detail/" + menuId;
    }

    /**
     * 특정 메뉴 상세 화면을 렌더링한다.
     *
     * @param menuId 메뉴 ID
     * @param model  뷰 모델
     * @return 상세 뷰 이름
     */
    @Operation(summary = "메뉴 상세 화면", description = "메뉴 상세 정보를 조회하여 화면을 렌더링한다.")
    @GetMapping("/menu/detail/{menuId}")
    public String detailStoreMenu(@PathVariable Long menuId, Model model) {
        MenuDetailDTO menu = menuService.MenuDetail(menuId);
        model.addAttribute("menu", menu);
        return "menu/detail";
    }

    /**
     * 특정 메뉴 수정 화면을 렌더링한다.
     *
     * @param menuId 메뉴 ID
     * @param model  뷰 모델
     * @return 수정 뷰 이름
     */
    @Operation(summary = "메뉴 수정 화면", description = "메뉴 상세를 수정 폼으로 변환하여 화면을 렌더링한다.")
    @GetMapping("/menu/modify/{menuId}")
    public String modifyStoreMenu(@PathVariable Long menuId, Model model) {
        MenuDetailDTO menu = menuService.MenuDetail(menuId);

        MenuModifyFormDTO form = MenuModifyFormDTO.builder()
                .menuId(menu.getMenuId())
                .menuCategoryId(menu.getMenuCategoryId())
                .menuShow(menu.getMenuShow())
                .menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName())
                .menuNameEnglish(menu.getMenuNameEnglish())
                .menuPrice(menu.getMenuPrice())
                .menuInformation(menu.getMenuInformation())
                .menuKcal(menu.getMenuKcal())
                .mainMaterials(menu.getMainMaterials())
                .sauceMaterials(menu.getSauceMaterials())
                .build();

        if (form.getMenuShow() == null) {
            form.setMenuShow(MenuShow.SHOW);
        }

        List<MenuCategory> categories = menuCategoryRepository.findSetAndLevel3Categories();

        List<MaterialSimpleDTO> mainOptions = materialRepository.findByCategory(MaterialCategory.BASE)
                .stream()
                .map(m -> new MaterialSimpleDTO(m.getId(), m.getName()))
                .toList();

        List<MaterialSimpleDTO> sauceOptions = materialRepository.findByCategory(MaterialCategory.SAUCE)
                .stream()
                .map(m -> new MaterialSimpleDTO(m.getId(), m.getName()))
                .toList();

        model.addAttribute("menuModifyFormDTO", form);
        model.addAttribute("menuCategories", categories);
        model.addAttribute("menuShowValues", MenuShow.values());
        model.addAttribute("mainOptions", mainOptions);
        model.addAttribute("sauceOptions", sauceOptions);
        model.addAttribute("units", RecipeUnit.values());

        return "menu/modify";
    }
}
