package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryAdjustment;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryAdjustment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 재고 조정 커스텀 리포지토리 구현.
 *
 * <p>QueryDSL 기반. 기간 파라미터는 부분 지정(from/to)도 허용한다.</p>
 */
@Repository
@RequiredArgsConstructor
public class InventoryAdjustmentRepositoryImpl implements InventoryAdjustmentRepositoryCustom {

    private final JPAQueryFactory qf;
    private static final QInventoryAdjustment a = QInventoryAdjustment.inventoryAdjustment;

    @Override
    public List<InventoryAdjustment> findByInventoryAndPeriod(Long inventoryId, LocalDateTime from, LocalDateTime to) {
        BooleanBuilder where = new BooleanBuilder();
        if (inventoryId != null) {
            where.and(a.inventory.id.eq(inventoryId));
        }
        if (from != null && to != null) {
            where.and(a.createdAt.between(from, to));
        } else if (from != null) {
            where.and(a.createdAt.goe(from));
        } else if (to != null) {
            where.and(a.createdAt.loe(to));
        }

        return qf.selectFrom(a)
                .where(where)
                .orderBy(a.createdAt.desc(), a.id.desc())
                .fetch();
    }

    @Override
    public BigDecimal sumQuantityByInventoryAndPeriod(Long inventoryId, LocalDateTime from, LocalDateTime to) {
        BooleanBuilder where = new BooleanBuilder();
        if (inventoryId != null) {
            where.and(a.inventory.id.eq(inventoryId));
        }
        if (from != null && to != null) {
            where.and(a.createdAt.between(from, to));
        } else if (from != null) {
            where.and(a.createdAt.goe(from));
        } else if (to != null) {
            where.and(a.createdAt.loe(to));
        }

        BigDecimal v = qf.select(a.difference.sum())
                .from(a)
                .where(where)
                .fetchOne();
        return v != null ? v : BigDecimal.ZERO;
    }

    @Override
    public LocalDateTime lastAdjustmentAtByInventory(Long inventoryId) {
        BooleanBuilder where = new BooleanBuilder();
        if (inventoryId != null) {
            where.and(a.inventory.id.eq(inventoryId));
        }
        return qf.select(a.createdAt.max())
                .from(a)
                .where(where)
                .fetchOne();
    }
}
