package com.boot.ict05_final_admin.domain.menu.service;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import com.boot.ict05_final_admin.domain.menu.dto.*;
import com.boot.ict05_final_admin.domain.menu.entity.*;

import com.boot.ict05_final_admin.domain.menu.repository.MenuCategoryRepository;
import com.boot.ict05_final_admin.domain.menu.repository.MenuRecipeRepository;
import com.boot.ict05_final_admin.domain.menu.repository.MenuRepository;
import com.boot.ict05_final_admin.domain.menu.repository.StoreMenuRepository;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import com.boot.ict05_final_admin.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * 본사 메뉴 도메인의 비즈니스 로직을 담당하는 서비스.
 *
 * <p>메뉴 등록, 수정, 상세/목록 조회, 레시피 저장, 신규 메뉴 생성 시 가맹점별 기본 상태 행 생성 등을 처리한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final MaterialRepository materialRepository;
    private final MenuRecipeRepository menuRecipeRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    // 🔹 가맹점 / 가맹점 메뉴
    private final StoreRepository storeRepository;
    private final StoreMenuRepository storeMenuRepository;

    /**
     * 메뉴 목록을 페이지 단위로 조회한다.
     *
     * @param menuSearchDTO 검색/필터 조건
     * @param pageable      페이징/정렬 정보
     * @return 페이징 처리된 메뉴 목록 DTO
     */
    public Page<MenuListDTO> selectAllStoreMenu(MenuSearchDTO menuSearchDTO, Pageable pageable) {
        var menus = menuRepository.listMenu(menuSearchDTO, pageable);

        log.info("rows={}", menus.getNumberOfElements());
        menus.getContent().forEach(m ->
                log.info("id={}, name={}, materials={}", m.getMenuId(), m.getMenuName(), m.getMaterialNames())
        );

        return menus;
    }

    /**
     * 새로운 메뉴를 등록한다.
     *
     * <p>메뉴 엔티티 저장 이후 레시피를 저장하고, 모든 가맹점에 기본 판매 상태(ON_SALE)로 {@code StoreMenu} 행을 생성한다.</p>
     *
     * @param dto 메뉴 등록 정보
     * @return 저장된 메뉴 ID
     * @throws IllegalArgumentException 카테고리가 없는 경우
     */
    @Transactional
    public Long insertStoreMenu(MenuWriteFormDTO dto) {
        MenuCategory category = menuCategoryRepository.findById(dto.getMenuCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음: " + dto.getMenuCategoryId()));

        MenuShow show = dto.getMenuShow() != null ? dto.getMenuShow() : MenuShow.HIDE;

        Menu menu = Menu.builder()
                .menuName(dto.getMenuName())
                .menuNameEnglish(dto.getMenuNameEnglish())
                .menuPrice(dto.getMenuPrice())
                .menuCode(dto.getMenuCode())
                .menuInformation(dto.getMenuInformation())
                .menuKcal(dto.getMenuKcal())
                .menuShow(show)
                .menuCategory(category)
                .build();

        // 저장 후 PK 확보
        menu = menuRepository.save(menu);

        // 레시피 저장 (자유 입력: material FK 없을 수 있음)
        saveRecipes(menu, dto.getMainMaterials(),  MenuRecipe.RecipeRole.MAIN);
        saveRecipes(menu, dto.getSauceMaterials(), MenuRecipe.RecipeRole.SAUCE);

        // 모든 가맹점에 StoreMenu 기본값 생성
        createStoreMenusForNewMenu(menu);

        return menu.getMenuId();
    }

    /**
     * 새로 등록된 본사 메뉴에 대해 모든 가맹점의 {@code StoreMenu} 행을 기본값 {@code ON_SALE}로 생성한다.
     *
     * @param menu 대상 메뉴
     */
    private void createStoreMenusForNewMenu(Menu menu) {
        List<Store> stores = storeRepository.findAll();

        int created = 0;
        for (Store store : stores) {
            // 중복 방지
            boolean exists = storeMenuRepository.existsByStoreAndMenu(store, menu);
            if (exists) continue;

            // ✅ 엔티티 타입으로 선언
            StoreMenu sm = StoreMenu.builder()
                    .store(store)
                    .menu(menu)
                    .storeMenuSoldout(StoreMenuSoldout.ON_SALE)
                    .build();

            storeMenuRepository.save(sm);
            created++;
        }

        log.info("[createStoreMenusForNewMenu] menuId={}, stores={}, createdRows={}",
                menu.getMenuId(), stores.size(), created);
    }


    /**
     * ID로 메뉴를 조회한다.
     *
     * @param menuId 메뉴 ID
     * @return 메뉴 엔티티, 없으면 {@code null}
     */
    public Menu findMenuById(Long menuId) {
        return menuRepository.findById(menuId).orElse(null);
    }

    /**
     * 기존 메뉴를 수정한다.
     *
     * <p>기본 필드를 갱신하고, 레시피는 전량 삭제 후 전달된 항목으로 재생성한다.</p>
     *
     * @param dto 수정 정보
     * @return 수정된 메뉴 엔티티
     * @throws IllegalArgumentException 메뉴 ID 없거나, 메뉴/카테고리가 존재하지 않는 경우
     */
    @Transactional
    public Menu menuModify(MenuModifyFormDTO dto) {
        if (dto == null || dto.getMenuId() == null) throw new IllegalArgumentException("메뉴 ID 없음");

        // menu_code 유니크 충돌 방지: 빈문자 -> null
        if (dto.getMenuCode() != null && dto.getMenuCode().trim().isEmpty()) {
            dto.setMenuCode(null);
        }

        Menu menu = findMenuById(dto.getMenuId());
        if (menu == null) throw new IllegalArgumentException("메뉴 없음");

        // 기본필드 갱신
        menu.setMenuName(dto.getMenuName());
        menu.setMenuNameEnglish(dto.getMenuNameEnglish());
        menu.setMenuPrice(dto.getMenuPrice());
        menu.setMenuCode(dto.getMenuCode());
        menu.setMenuInformation(dto.getMenuInformation());
        menu.setMenuKcal(dto.getMenuKcal());

        if (dto.getMenuShow() != null) {
            menu.setMenuShow(dto.getMenuShow());
        }

        // 카테고리: ID 있을 때만 처리
        if (dto.getMenuCategoryId() != null) {
            menu.setMenuCategory(
                    menuCategoryRepository.findById(dto.getMenuCategoryId())
                            .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"))
            );
        }

        // 레시피: 전량 삭제 후 재등록 (MAIN / SAUCE)
        menuRecipeRepository.deleteAllByMenu(menu);

        if (dto.getMainMaterials() != null) {
            int sort = 0;
            for (RecipeItemDTO it : dto.getMainMaterials()) {
                if (it == null || it.getMaterialId() == null || it.getRecipeQty() == null) continue;
                if (it.getRecipeQty().doubleValue() <= 0) continue;
                if (Boolean.TRUE.equals(it.getDeleteFlag())) continue;

                Material mat = materialRepository.findById(it.getMaterialId())
                        .orElseThrow(() -> new IllegalArgumentException("재료 없음"));

                MenuRecipe r = new MenuRecipe();
                r.setMenu(menu);
                r.setMaterial(mat);
                r.setRecipeRole(MenuRecipe.RecipeRole.MAIN);
                r.setRecipeQty(it.getRecipeQty());
                r.setRecipeUnit(it.getRecipeUnit());
                r.setRecipeSort(sort++);

                String itemName = (it.getItemName() != null && StringUtils.hasText(it.getItemName()))
                        ? it.getItemName()
                        : mat.getName();
                r.setRecipeItemName(itemName);

                menuRecipeRepository.save(r);
            }
        }

        if (dto.getSauceMaterials() != null) {
            int sort = 0;
            for (RecipeItemDTO it : dto.getSauceMaterials()) {
                if (it == null || it.getMaterialId() == null || it.getRecipeQty() == null) continue;
                if (it.getRecipeQty().doubleValue() <= 0) continue;
                if (Boolean.TRUE.equals(it.getDeleteFlag())) continue;

                Material mat = materialRepository.findById(it.getMaterialId())
                        .orElseThrow(() -> new IllegalArgumentException("재료 없음"));

                MenuRecipe r = new MenuRecipe();
                r.setMenu(menu);
                r.setMaterial(mat);
                r.setRecipeRole(MenuRecipe.RecipeRole.SAUCE);
                r.setRecipeQty(it.getRecipeQty());
                r.setRecipeUnit(it.getRecipeUnit());
                r.setRecipeSort(sort++);

                String itemName = (it.getItemName() != null && StringUtils.hasText(it.getItemName()))
                        ? it.getItemName()
                        : mat.getName();
                r.setRecipeItemName(itemName);

                menuRecipeRepository.save(r);
            }
        }

        return menu;
    }

    /**
     * 메뉴 상세 정보를 조회한다.
     *
     * @param menuId 메뉴 ID
     * @return 상세 DTO
     * @throws IllegalArgumentException 메뉴가 존재하지 않는 경우
     */
    public MenuDetailDTO MenuDetail(Long menuId) {
        Menu m = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("menu not found: " + menuId));

        // 주재료
        List<RecipeItemDTO> mains = m.getRecipe().stream()
                .filter(r -> r.getRecipeRole() == MenuRecipe.RecipeRole.MAIN)
                .sorted(Comparator.comparing(MenuRecipe::getRecipeSort))
                .map(r -> {
                    RecipeItemDTO d = new RecipeItemDTO();
                    d.setMaterialId(r.getMaterial() != null ? r.getMaterial().getId() : null);
                    d.setItemName(r.getRecipeItemName());
                    d.setRecipeQty(r.getRecipeQty());
                    d.setRecipeUnit(r.getRecipeUnit());
                    d.setRecipeSortNo(r.getRecipeSort());
                    return d;
                }).toList();

        // 소스
        List<RecipeItemDTO> sauces = m.getRecipe().stream()
                .filter(r -> r.getRecipeRole() == MenuRecipe.RecipeRole.SAUCE)
                .sorted(Comparator.comparing(MenuRecipe::getRecipeSort))
                .map(r -> {
                    RecipeItemDTO d = new RecipeItemDTO();
                    d.setMaterialId(r.getMaterial() != null ? r.getMaterial().getId() : null);
                    d.setItemName(r.getRecipeItemName());
                    d.setRecipeQty(r.getRecipeQty());
                    d.setRecipeUnit(r.getRecipeUnit());
                    d.setRecipeSortNo(r.getRecipeSort());
                    return d;
                }).toList();

        MenuCategory category = m.getMenuCategory();
        Long categoryId = (category != null) ? category.getMenuCategoryId() : null;
        String categoryName = (category != null) ? category.getMenuCategoryName() : null;

        return MenuDetailDTO.builder()
                .menuId(m.getMenuId())
                .menuCategoryId(categoryId)
                .menuCategory(category)
                .menuCategoryName(categoryName)
                .menuShow(m.getMenuShow())
                .menuCode(m.getMenuCode())
                .menuName(m.getMenuName())
                .menuNameEnglish(m.getMenuNameEnglish())
                .menuPrice(m.getMenuPrice())
                .menuInformation(m.getMenuInformation())
                .menuKcal(m.getMenuKcal())
                .mainMaterials(mains)
                .sauceMaterials(sauces)
                .build();
    }

    /**
     * 레시피 저장 유틸리티.
     *
     * <p>유효성 검사 후 {@code MenuRecipe}를 생성하여 저장한다.</p>
     *
     * @param menu  대상 메뉴
     * @param items 레시피 항목 목록
     * @param role  레시피 역할(MAIN/SAUCE)
     */
    private void saveRecipes(Menu menu,
                             List<RecipeItemDTO> items,
                             MenuRecipe.RecipeRole role) {

        if (items == null || items.isEmpty()) return;

        int sort = 1;
        for (RecipeItemDTO it : items) {
            if (it == null) continue;

            if (it.getRecipeQty() == null || it.getRecipeQty().signum() <= 0) continue;
            if (it.getRecipeUnit() == null) continue;

            Material material = null;
            if (it.getMaterialId() != null) {
                material = materialRepository.findById(it.getMaterialId())
                        .orElse(null);
            }

            String itemName = it.getItemName();
            if (itemName == null || itemName.isBlank()) {
                itemName = (material != null ? material.getName() : "기타");
            }

            MenuRecipe recipe = MenuRecipe.builder()
                    .menu(menu)
                    .material(material)
                    .recipeItemName(itemName)
                    .recipeQty(it.getRecipeQty())
                    .recipeUnit(it.getRecipeUnit())
                    .recipeSort(sort++)
                    .recipeRole(role)
                    .build();

            menuRecipeRepository.save(recipe);
        }
    }

    /** BigDecimal NVL 유틸. */
    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

}
