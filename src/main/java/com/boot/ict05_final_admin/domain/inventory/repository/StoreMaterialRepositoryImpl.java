package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
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

@Repository
@RequiredArgsConstructor
public class StoreMaterialRepositoryImpl implements StoreMaterialRepositoryCustom {

    private final JPAQueryFactory qf;

    private static final QStoreMaterial sm = QStoreMaterial.storeMaterial;
    private static final QMaterial      m  = QMaterial.material;
    private static final QStore         st = QStore.store;

    @Override
    public Page<StoreMaterialListDTO> listStoreMaterial(final StoreMaterialSearchDTO dto,
                                                        final Pageable pageable) {
        Sort sort = (pageable != null && pageable.getSort().isSorted())
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "id");
        Pageable p = PageRequest.of(
                pageable != null ? pageable.getPageNumber() : 0,
                pageable != null ? pageable.getPageSize() : 20,
                sort
        );

        List<StoreMaterialListDTO> content = qf
                .select(Projections.fields(StoreMaterialListDTO.class,
                        sm.id,
                        // HQ면 material.*, 아니면 storeMaterial.* 로 fallback
                        Expressions.stringTemplate("COALESCE({0}, {1})", m.code, sm.code).as("code"),
                        Expressions.stringTemplate("COALESCE({0}, {1})", m.name, sm.name).as("name"),
                        m.materialCategory.as("materialCategory"),
                        Expressions.stringTemplate("COALESCE({0}, {1})", m.baseUnit, sm.baseUnit).as("baseUnit"),
                        Expressions.stringTemplate("COALESCE({0}, {1})", m.salesUnit, sm.salesUnit).as("salesUnit"),
                        // ← conversionRate 는 Material 만 사용 (StoreMaterial 에는 없음)
                        m.conversionRate.as("conversionRate"),
                        Expressions.stringTemplate("COALESCE({0}, {1})", m.supplier, sm.supplier).as("supplier"),
                        sm.status.as("status"),
                        sm.isHqMaterial.as("isHqMaterial"),
                        st.name.as("storeName")
                ))
                .from(sm)
                .leftJoin(sm.material, m)  // 비-HQ 대응
                .join(sm.store, st)
                .where(filter(dto))
                .orderBy(sm.id.desc())
                .offset(p.getOffset())
                .limit(p.getPageSize())
                .fetch();

        long total = countStoreMaterial(dto);
        return new PageImpl<>(content, p, total);
    }

    @Override
    public long countStoreMaterial(final StoreMaterialSearchDTO dto) {
        Long total = qf.select(sm.count())
                .from(sm)
                .leftJoin(sm.material, m)
                .join(sm.store, st)
                .where(filter(dto))
                .fetchOne();
        return total != null ? total : 0L;
    }

    private BooleanExpression filter(final StoreMaterialSearchDTO dto) {
        BooleanExpression w = Expressions.TRUE.isTrue();
        if (dto == null) return w;

        if (dto.getStoreId() != null) {
            w = w.and(st.id.eq(dto.getStoreId()));
        }
        if (dto.getIsHqMaterial() != null) {
            w = w.and(sm.isHqMaterial.eq(dto.getIsHqMaterial()));
        }
        if (dto.getStatus() != null) {
            w = w.and(m.materialStatus.eq(dto.getStatus()).or(sm.status.eq(dto.getStatus())));
        }
        if (dto.getS() != null && !dto.getS().isBlank()) {
            String s = dto.getS();
            w = w.and(
                    m.name.containsIgnoreCase(s)
                            .or(sm.name.containsIgnoreCase(s))
                            .or(m.materialCategory.stringValue().containsIgnoreCase(s))
            );
        }
        return w;
    }
}
