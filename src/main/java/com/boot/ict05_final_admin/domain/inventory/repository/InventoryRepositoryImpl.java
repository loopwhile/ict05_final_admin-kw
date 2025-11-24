package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 본사 재고(Inventory) 커스텀 리포지토리 구현체(QueryDSL).
 *
 * <p>재고 목록 화면에 필요한 DTO 기반 조회 및 검색 조건별 카운트,
 * 재료별 적정 재고량 일괄 갱신을 담당한다.</p>
 */
@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepositoryCustom {

    private final JPAQueryFactory qf;

    private static final QInventory inv = QInventory.inventory;
    private static final QMaterial  m   = QMaterial.material;

    /**
     * 본사 재고 목록 페이지 조회.
     *
     * <p>검색 조건 DTO와 페이징 정보를 이용해 {@link InventoryListDTO} 페이지를 반환한다.
     * 정렬 정보가 전달되지 않은 경우, 기본 정렬은 {@code updateDate DESC}를 사용한다.</p>
     *
     * <p>주의: 인벤토리 상태는 DB 값 대신 {@link InventoryStatus#calculate}를 통해
     * 조회 결과에서 1차로 재계산해 세팅한다.</p>
     *
     * @param dto      검색 조건 DTO
     * @param pageable 페이징/정렬 정보
     * @return 재고 목록 DTO 페이지
     */
    @Override
    public Page<InventoryListDTO> listInventory(InventorySearchDTO dto, Pageable pageable) {
        // 정렬 힌트: 클라이언트가 넘기지 않으면 updateDate DESC
        Sort sort = (pageable != null && pageable.getSort().isSorted())
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "updateDate");

        Pageable p = PageRequest.of(
                pageable != null ? pageable.getPageNumber() : 0,
                pageable != null ? pageable.getPageSize() : 20,
                sort
        );

        List<InventoryListDTO> content = qf
                .select(Projections.fields(InventoryListDTO.class,
                        inv.id,
                        m.id.as("materialId"),
                        m.name.as("materialName"),
                        m.materialCategory.stringValue().as("categoryName"),
                        inv.quantity,
                        m.optimalQuantity.as("optimalQuantity"),
                        m.salesUnit.as("materialSalesUnit"),
                        inv.status,
                        inv.updateDate))
                .from(inv)
                .join(inv.material, m)
                .where(filter(dto))
                .orderBy(inv.updateDate.desc(), inv.id.desc())
                .offset(p.getOffset())
                .limit(p.getPageSize())
                .fetch();

        // 조회 시점 기준 상태 재계산
        for (InventoryListDTO row : content) {
            row.setStatus(InventoryStatus.calculate(row.getQuantity(), row.getOptimalQuantity()));
        }

        long total = countInventory(dto);
        return new PageImpl<>(content, p, total);
    }

    /**
     * 본사 재고 목록 총 건수 조회.
     *
     * <p>{@link #listInventory(InventorySearchDTO, Pageable)}와 동일한 검색 조건으로
     * 총 행 수를 반환한다.</p>
     *
     * @param dto 검색 조건 DTO
     * @return 검색 조건에 매칭되는 재고 행 총 건수
     */
    @Override
    public long countInventory(InventorySearchDTO dto) {
        Long total = qf.select(inv.count())
                .from(inv)
                .join(inv.material, m)
                .where(filter(dto))
                .fetchOne();
        return total != null ? total : 0L;
    }

    // ----- helpers -----

    /**
     * 검색 조건 DTO를 기반으로 재고 목록 필터링 조건을 생성한다.
     *
     * <ul>
     *   <li>s: 재료명, 카테고리 문자열 검색</li>
     *   <li>status: 인벤토리 상태 필터</li>
     * </ul>
     *
     * @param dto 검색 조건 DTO
     * @return QueryDSL BooleanExpression (null 허용 없이 TRUE 누적)
     */
    private BooleanExpression filter(InventorySearchDTO dto) {
        BooleanExpression w = Expressions.TRUE.isTrue();
        if (dto == null) return w;

        // 검색어(s): 재료명 / 카테고리명
        if (dto.getS() != null && !dto.getS().isBlank()) {
            String s = dto.getS();
            w = w.and(
                    m.name.containsIgnoreCase(s)
                            .or(m.materialCategory.stringValue().containsIgnoreCase(s))
            );
        }

        // 상태
        if (dto.getStatus() != null) {
            w = w.and(inv.status.eq(dto.getStatus()));
        }

        return w;
    }

    /**
     * 단일 재료(materialId)에 대한 인벤토리의 적정 재고량을 일괄 갱신한다.
     *
     * <p>본사 재료 마스터의 적정 재고량이 변경되었을 때,
     * 해당 재료를 참조하는 인벤토리(inventory_optimal_quantity)를
     * 새 값으로 동기화하기 위해 사용한다.</p>
     *
     * @param materialId    대상 재료 ID
     * @param newOptimalQty 재설정할 적정 재고량(DECIMAL(15,3) 가정)
     * @return 업데이트된 인벤토리 행 수
     */
    @Override
    public long updateOptimalQuantityByMaterialId(final Long materialId, final BigDecimal newOptimalQty) {
        final QInventory inv = QInventory.inventory;
        return qf
                .update(inv)
                .set(inv.optimalQuantity, newOptimalQty)
                .where(inv.material.id.eq(materialId))
                .execute();
    }
}
