package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QStoreInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QStoreMaterial;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 가맹점 재고 커스텀 구현(QueryDSL).
 */
@Repository
@RequiredArgsConstructor
public class StoreInventoryRepositoryImpl implements StoreInventoryRepositoryCustom {

    private final JPAQueryFactory qf;

    private static final QStoreInventory si = QStoreInventory.storeInventory;
    private static final QStoreMaterial  sm = QStoreMaterial.storeMaterial;
    private static final QStore          st = QStore.store;

    @Override
    public Page<StoreInventoryListDTO> listStoreInventory(final StoreInventorySearchDTO dto,
                                                          final Pageable pageable) {
        Sort sort = (pageable != null && pageable.getSort().isSorted())
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "id");
        Pageable p = PageRequest.of(
                pageable != null ? pageable.getPageNumber() : 0,
                pageable != null ? pageable.getPageSize() : 20,
                sort
        );

        List<StoreInventoryListDTO> rows = qf
                .select(Projections.fields(StoreInventoryListDTO.class,
                        si.id.as("id"),
                        st.id.as("storeId"),
                        st.name.as("storeName"),
                        // materialName: HQ 재료면 material.name, 아니면 storeMaterial.name
                        Expressions.stringTemplate("COALESCE({0}, {1})",
                                sm.material.name, sm.name).as("materialName"),
                        // categoryName: HQ 재료면 enum string, 아니면 storeMaterial.category
                        Expressions.stringTemplate("COALESCE({0}, {1})",
                                sm.material.materialCategory.stringValue(), sm.category).as("categoryName"),
                        si.quantity.as("quantity"),
                        si.optimalQuantity.as("optimalQuantity"),
                        si.status.as("status"),
                        si.updateDate.as("updateDate")
                ))
                .from(si)
                .join(si.storeMaterial, sm)
                .leftJoin(sm.material)   // 비-HQ 대비
                .join(si.store, st)
                .where(filter(dto))
                .orderBy(si.id.desc())
                .offset(p.getOffset())
                .limit(p.getPageSize())
                .fetch();

        long total = countStoreInventory(dto);
        return new PageImpl<>(rows, p, total);
    }

    @Override
    public long countStoreInventory(final StoreInventorySearchDTO dto) {
        Long total = qf.select(si.count())
                .from(si)
                .join(si.storeMaterial, sm)
                .leftJoin(sm.material)
                .join(si.store, st)
                .where(filter(dto))
                .fetchOne();
        return total != null ? total : 0L;
    }

    // ----- helpers -----

    private BooleanExpression filter(final StoreInventorySearchDTO dto) {
        BooleanExpression where = Expressions.TRUE.isTrue();
        if (dto == null) return where;

        // 가맹점 ID
        if (dto.getStoreId() != null) {
            where = where.and(st.id.eq(dto.getStoreId()));
        }

        // 상태
        if (dto.getStatus() != null) {
            where = where.and(si.status.eq(dto.getStatus()));
        }

        // 검색어: HQ/비-HQ 모두 대응 (COALESCE와 동일한 의미)
        if (dto.getS() != null && !dto.getS().isBlank()) {
            String s = dto.getS();
            where = where.and(
                    // 재료명
                    sm.material.name.containsIgnoreCase(s)
                            .or(sm.name.containsIgnoreCase(s))
                            // 카테고리(enum 문자열 or 커스텀 카테고리)
                            .or(sm.material.materialCategory.stringValue().containsIgnoreCase(s))
                            .or(sm.category.containsIgnoreCase(s))
            );
        }

        return where;
    }
}
