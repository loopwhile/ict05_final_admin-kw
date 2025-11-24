package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

/**
 * 재료(Material) 커스텀 리포지토리 구현(QueryDSL).
 */
@Repository
@RequiredArgsConstructor
public class MaterialRepositoryImpl implements MaterialRepositoryCustom {

    private final JPAQueryFactory qf;

    private static final QMaterial m = QMaterial.material;

    @Override
    public Page<MaterialListDTO> listMaterial(final MaterialSearchDTO dto, final Pageable pageable) {
        // 정렬 힌트: 미지정 시 id DESC
        Sort sort = (pageable != null && pageable.getSort().isSorted())
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "id");
        Pageable p = PageRequest.of(
                pageable != null ? pageable.getPageNumber() : 0,
                pageable != null ? pageable.getPageSize() : 20,
                sort
        );

        List<MaterialListDTO> content = qf
                .select(Projections.fields(MaterialListDTO.class,
                        m.id,
                        m.code,
                        m.materialCategory,
                        m.name,
                        m.baseUnit,
                        m.salesUnit,
                        m.conversionRate,
                        m.supplier,
                        m.materialTemperature,
                        m.materialStatus
                ))
                .from(m)
                .where(filter(dto))
                .orderBy(m.id.desc())
                .offset(p.getOffset())
                .limit(p.getPageSize())
                .fetch();

        Long total = qf.select(m.count())
                .from(m)
                .where(filter(dto))
                .fetchOne();

        return new PageImpl<>(content, p, total != null ? total : 0L);
    }

    @Override
    public long countMaterial(final MaterialSearchDTO dto) {
        Long total = qf.select(m.count())
                .from(m)
                .where(filter(dto))
                .fetchOne();
        return total != null ? total : 0L;
    }

    // ----- helpers -----

    /**
     * 검색 조건 필터.
     * - type: all/title/content/category
     * - s: 키워드(카테고리는 description 매핑)
     * - status: MaterialStatus
     */
    private BooleanExpression filter(final MaterialSearchDTO dto) {
        if (dto == null) return Expressions.TRUE.isTrue();

        BooleanExpression where = Expressions.TRUE.isTrue();

        // 키워드/타입
        String type = dto.getType() == null ? "all" : dto.getType();
        String keyword = dto.getS();

        if (keyword != null && !keyword.isBlank()) {
            // 한글 설명 -> enum name 매핑
            String matchedCategoryName = Arrays.stream(MaterialCategory.values())
                    .filter(c -> {
                        String desc = c.getDescription();
                        return desc != null && desc.contains(keyword);
                    })
                    .map(Enum::name)
                    .findFirst()
                    .orElse(null);

            switch (type) {
                case "title" -> where = where.and(m.name.containsIgnoreCase(keyword));
                case "content" -> where = where.and(m.supplier.containsIgnoreCase(keyword));
                case "category" -> {
                    if (matchedCategoryName != null) {
                        where = where.and(m.materialCategory.stringValue().eq(matchedCategoryName));
                    } else {
                        // 매칭 안 되면 결과 없음
                        where = where.and(Expressions.FALSE.isTrue());
                    }
                }
                default -> {
                    BooleanExpression kw = m.name.containsIgnoreCase(keyword)
                            .or(m.supplier.containsIgnoreCase(keyword));
                    if (matchedCategoryName != null) {
                        kw = kw.or(m.materialCategory.stringValue().eq(matchedCategoryName));
                    }
                    where = where.and(kw);
                }
            }
        }

        if (dto.getStatus() != null) {
            where = where.and(m.materialStatus.eq(dto.getStatus()));
        }

        return where;
    }
}
