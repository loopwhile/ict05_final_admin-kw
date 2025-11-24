package com.boot.ict05_final_admin.domain.menu.repository;

import com.boot.ict05_final_admin.domain.menu.dto.MenuListDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuSearchDTO;
import com.boot.ict05_final_admin.domain.menu.entity.QMenu;
import com.boot.ict05_final_admin.domain.menu.entity.QMenuCategory;
import com.boot.ict05_final_admin.domain.menu.entity.QMenuRecipe;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 메뉴 목록 조회 커스텀 리포지토리 구현체.
 *
 * <p>QueryDSL을 사용하여 검색/필터/정렬/페이징을 수행하고
 * 필요한 경우 재료명까지 조인하여 한 번에 조회한다.</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 메뉴 목록을 검색/필터/정렬/페이징 조건과 함께 조회한다.
     *
     * @param dto      검색/필터 조건
     * @param pageable 페이징 및 정렬 정보
     * @return 페이지 객체(요약 DTO 목록 포함)
     */
    @Override
    public Page<MenuListDTO> listMenu(MenuSearchDTO dto, Pageable pageable) {
        if (dto == null) dto = new MenuSearchDTO();

        QMenu menu = QMenu.menu;
        QMenuCategory category = QMenuCategory.menuCategory;
        QMenuRecipe recipe = QMenuRecipe.menuRecipe;
        QMaterial material = QMaterial.material;

        BooleanExpression where = andAll(
                eqNameOrInfo(dto, menu),
                eqCategory(dto, menu),
                eqShow(dto, menu)
        );

        Sort sort = (pageable.getSort().isSorted())
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "menuId");

        // 1) 페이지 대상 ID만 먼저 조회
        List<Long> pageIds = queryFactory
                .select(menu.menuId)
                .from(menu)
                .leftJoin(menu.menuCategory, category)
                .where(where)
                .orderBy(toOrderSpec(menu, sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        log.info("[listMenu] pageIds size={}, ids={}", pageIds.size(), pageIds);

        if (pageIds.isEmpty()) {
            log.info("[listMenu] pageIds empty -> return empty page");
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2) 상세 + 재료 일괄 조회 (선택된 ID 한정)
        var rows = queryFactory
                .select(Projections.tuple(
                        menu.menuId,
                        menu.menuShow,
                        menu.menuName,
                        menu.menuCode,
                        category.menuCategoryId,
                        category.menuCategoryName,
                        menu.menuPrice,
                        menu.menuKcal,
                        material.name
                ))
                .from(menu)
                .leftJoin(menu.menuCategory, category)
                .leftJoin(menu.recipe, recipe)
                .leftJoin(recipe.material, material)
                .where(menu.menuId.in(pageIds))
                .orderBy(toOrderSpec(menu, sort))
                .fetch();

        log.info("[listMenu] rows fetched={}", rows.size());

        Map<Long, MenuListDTO> map = new LinkedHashMap<>();
        for (var t : rows) {
            Long id = t.get(menu.menuId);
            MenuListDTO v = map.computeIfAbsent(id, k -> {
                MenuListDTO d = new MenuListDTO();
                d.setMenuId(t.get(menu.menuId));
                d.setMenuShow(t.get(menu.menuShow));
                d.setMenuName(t.get(menu.menuName));
                d.setMenuCode(t.get(menu.menuCode));
                d.setMenuCategoryId(t.get(category.menuCategoryId));
                d.setMenuCategoryName(t.get(category.menuCategoryName));
                d.setMenuPrice(t.get(menu.menuPrice));
                d.setMenuKcal(t.get(menu.menuKcal));
                d.setMaterialNames(new ArrayList<>());
                return d;
            });
            String mName = t.get(material.name);
            if (mName != null) v.getMaterialNames().add(mName);
        }
        List<MenuListDTO> content = new ArrayList<>(map.values());

        // 3) Count (가벼운 버전: 불필요 조인 제거)
        Long total = queryFactory
                .select(menu.menuId.countDistinct())
                .from(menu)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 이름 또는 설명 조건을 생성한다.
     *
     * @param dto  검색 조건
     * @param menu QMenu
     * @return 조건 식(없으면 null)
     */
    private BooleanExpression eqNameOrInfo(MenuSearchDTO dto, QMenu menu) {
        String kw = dto.getS();
        if (!StringUtils.hasText(kw)) return null;

        String type = Optional.ofNullable(dto.getType()).orElse("all");
        return switch (type) {
            case "name" -> menu.menuName.containsIgnoreCase(kw);
            default -> menu.menuName.containsIgnoreCase(kw);
        };
    }

    /**
     * 카테고리 필터 조건을 생성한다.
     *
     * @param dto  검색 조건
     * @param menu QMenu
     * @return 조건 식(없으면 null)
     */
    private BooleanExpression eqCategory(MenuSearchDTO dto, QMenu menu) {
        if (dto.getMenuCategoryId() == null || dto.getMenuCategoryId() == 0) return null;
        return menu.menuCategory.menuCategoryId.eq(dto.getMenuCategoryId());
    }

    /**
     * 판매 상태 필터 조건을 생성한다.
     *
     * @param dto  검색 조건
     * @param menu QMenu
     * @return 조건 식(없으면 null)
     */
    private BooleanExpression eqShow(MenuSearchDTO dto, QMenu menu) {
        if (dto.getMenuShow() == null) return null;
        return menu.menuShow.eq(dto.getMenuShow());
    }

    /**
     * 여러 조건을 AND로 결합한다.
     *
     * @param exps 조건 배열
     * @return 결합된 조건(없으면 null)
     */
    private BooleanExpression andAll(BooleanExpression... exps) {
        BooleanExpression result = null;
        for (BooleanExpression exp : exps) {
            if (exp == null) continue;
            result = (result == null) ? exp : result.and(exp);
        }
        return result;
    }

    /**
     * Pageable의 Sort를 QueryDSL {@code OrderSpecifier[]}로 변환한다.
     *
     * @param menu QMenu
     * @param sort 정렬 정보
     * @return OrderSpecifier 배열
     */
    private com.querydsl.core.types.OrderSpecifier<?>[] toOrderSpec(QMenu menu, Sort sort) {
        return sort.stream()
                .map(order -> {
                    com.querydsl.core.types.Order direction = order.isAscending()
                            ? com.querydsl.core.types.Order.ASC
                            : com.querydsl.core.types.Order.DESC;
                    return switch (order.getProperty()) {
                        case "menuId"    -> new com.querydsl.core.types.OrderSpecifier<>(direction, menu.menuId);
                        case "menuName"  -> new com.querydsl.core.types.OrderSpecifier<>(direction, menu.menuName);
                        case "menuPrice" -> new com.querydsl.core.types.OrderSpecifier<>(direction, menu.menuPrice);
                        case "menuKcal"  -> new com.querydsl.core.types.OrderSpecifier<>(direction, menu.menuKcal);
                        default          -> new com.querydsl.core.types.OrderSpecifier<>(com.querydsl.core.types.Order.DESC, menu.menuId);
                    };
                })
                .toArray(com.querydsl.core.types.OrderSpecifier[]::new);
    }
}
