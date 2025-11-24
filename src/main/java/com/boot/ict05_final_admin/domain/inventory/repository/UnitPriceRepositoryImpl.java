package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.QUnitPrice;
import com.boot.ict05_final_admin.domain.inventory.entity.UnitPrice;
import com.boot.ict05_final_admin.domain.inventory.entity.UnitPriceType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 단가 이력 커스텀 구현(QueryDSL).
 *
 * <p>유효 기간(validFrom <= ts < validTo(NULL 허용)) 기준으로 최신 값을 조회한다.</p>
 */
@Repository
@RequiredArgsConstructor
public class UnitPriceRepositoryImpl implements UnitPriceRepositoryCustom {

    private final JPAQueryFactory qf;
    private static final QUnitPrice p = QUnitPrice.unitPrice;

    @Override
    public Optional<UnitPrice> findLatestPurchasePrice(Long materialId, LocalDateTime at) {
        LocalDateTime ts = at != null ? at : LocalDateTime.now();

        UnitPrice row = qf.selectFrom(p)
                .where(
                        p.material.id.eq(materialId),
                        p.type.eq(UnitPriceType.PURCHASE),
                        p.validFrom.loe(ts),
                        p.validTo.isNull().or(p.validTo.gt(ts))
                )
                .orderBy(p.validFrom.desc(), p.id.desc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(row);
    }

    @Override
    public List<UnitPrice> historyPurchasePrice(Long materialId, int limit) {
        return qf.selectFrom(p)
                .where(
                        p.material.id.eq(materialId),
                        p.type.eq(UnitPriceType.PURCHASE)
                )
                .orderBy(p.validFrom.desc(), p.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Optional<UnitPrice> findLatestSellingPrice(Long materialId, LocalDateTime at) {
        LocalDateTime ts = at != null ? at : LocalDateTime.now();

        UnitPrice row = qf.selectFrom(p)
                .where(
                        p.material.id.eq(materialId),
                        p.type.eq(UnitPriceType.SELLING),
                        p.validFrom.loe(ts),
                        p.validTo.isNull().or(p.validTo.gt(ts))
                )
                .orderBy(p.validFrom.desc(), p.id.desc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(row);
    }

    @Override
    public List<UnitPrice> historySellingPrice(Long materialId, int limit) {
        return qf.selectFrom(p)
                .where(
                        p.material.id.eq(materialId),
                        p.type.eq(UnitPriceType.SELLING)
                )
                .orderBy(p.validFrom.desc(), p.id.desc())
                .limit(limit)
                .fetch();
    }
}
