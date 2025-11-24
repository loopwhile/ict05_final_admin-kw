package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryBatch;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 배치(LOT) 커스텀 리포지토리 구현.
 *
 * <p>QueryDSL 기반 정렬/필터 최적화.</p>
 */
@Repository
@RequiredArgsConstructor
public class InventoryBatchRepositoryImpl implements InventoryBatchRepositoryCustom {

    private final JPAQueryFactory qf;
    private static final QInventoryBatch b = QInventoryBatch.inventoryBatch;

    @Override
    public List<InventoryBatch> findHqBatchesForMaterial(Long materialId) {
        List<InventoryBatch> rows = qf
                .selectFrom(b)
                .where(
                        b.material.id.eq(materialId),
                        b.store.isNull(),
                        b.quantity.gt(BigDecimal.ZERO)
                )
                .orderBy(
                        // 만료일 오름차순, NULL(미지정)은 뒤로
                        new OrderSpecifier<>(Order.ASC, b.expirationDate, OrderSpecifier.NullHandling.NullsLast),
                        // 동일 만료일이면 먼저 들어온 배치부터
                        b.receivedDate.asc(),
                        // 동일 입고일이면 PK 오름차순
                        b.id.asc()
                )
                .fetch();
        return rows != null ? rows : java.util.Collections.emptyList();
    }

    @Override
    public List<InventoryBatch> findFifoCandidates(Long materialId) {
        // 정책 동일 — 필요 시 조건/정렬 분리 가능
        return findHqBatchesForMaterial(materialId);
    }
}
