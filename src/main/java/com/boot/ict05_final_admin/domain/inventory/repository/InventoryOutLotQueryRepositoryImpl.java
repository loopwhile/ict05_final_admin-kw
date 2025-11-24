package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.BatchOutRowDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryOut;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryOutLot;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

/**
 * 출고-LOT 커스텀 쿼리 구현(QueryDSL).
 */
@Repository
@RequiredArgsConstructor
public class InventoryOutLotQueryRepositoryImpl implements InventoryOutLotQueryRepository {

    private final JPAQueryFactory qf;
    private static final QInventoryOutLot ol = QInventoryOutLot.inventoryOutLot;
    private static final QInventoryOut    o  = QInventoryOut.inventoryOut;

    @Override
    public Page<BatchOutRowDTO> pageOutHistoryByBatch(final Long batchId, final Pageable pageable) {
        Long total = qf.select(ol.id.count())
                .from(ol)
                .where(ol.batch.id.eq(batchId))
                .fetchOne();
        long totalCount = (total != null) ? total : 0L;

        var rows = qf.select(Projections.constructor(
                        BatchOutRowDTO.class,
                        o.id,          // outId
                        o.store.id,    // storeId
                        o.store.name,  // storeName
                        ol.quantity,   // qty
                        o.outDate      // outDate
                ))
                .from(ol)
                .join(ol.out, o)
                .where(ol.batch.id.eq(batchId))
                .orderBy(o.outDate.desc(), ol.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(rows, pageable, totalCount);
    }
}
