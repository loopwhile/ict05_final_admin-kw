package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryOutLot;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 출고-LOT 커스텀 구현(QueryDSL).
 */
@Repository
@RequiredArgsConstructor
public class InventoryOutLotRepositoryImpl implements InventoryOutLotRepositoryCustom {

    private final JPAQueryFactory qf;

    private static final QInventoryOutLot l = QInventoryOutLot.inventoryOutLot;
    private static final QInventoryBatch  b = QInventoryBatch.inventoryBatch;

    @Override
    public List<InventoryOutLot> findByOutId(Long outId) {
        return qf.selectFrom(l)
                .leftJoin(l.batch, b).fetchJoin()  // LOT 상세(배치 정보) 함께 로딩
                .where(l.out.id.eq(outId))
                .orderBy(l.id.asc())
                .fetch();
    }
}
