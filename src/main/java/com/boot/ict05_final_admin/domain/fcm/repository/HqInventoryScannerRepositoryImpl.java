package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.dto.HqExpireSoonCandidate;
import com.boot.ict05_final_admin.domain.fcm.dto.HqStockLowCandidate;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Primary
@Slf4j
public class HqInventoryScannerRepositoryImpl implements HqInventoryScannerRepository {

    private final JPAQueryFactory query;

    /**
     * (1) 재고부족 판단 로직
     *  - threshold(임계/적정 수량) 계산 우선순위:
     *      HQ 인벤토리(inv.optimalQuantity)가 있으면 우선 사용,
     *      없으면 재료 기본값(m.optimalQuantity) 사용.
     *    => COALESCE(inv.optimalQuantity, m.optimalQuantity)
     *  - 부족 판정: inv.quantity < threshold
     *  - 정렬 기준: (qty / threshold) 오름차순 → 가장 부족한(비율이 낮은) 항목 우선
     *    ratio = qty / NULLIF(threshold, 0)
     *    (threshold가 0 또는 null이면 0으로 나누기 방지: NULLIF로 회피 → ratio가 null이면 정렬상 뒤쪽)
     */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<HqStockLowCandidate> findStockLow(int maxRows) {

        QInventory inv = QInventory.inventory;
        QMaterial m      = QMaterial.material;

        // threshold := COALESCE(inv.optimalQuantity, m.optimalQuantity)
        //  - inv(본사 인벤토리)의 적정수량이 우선, 없으면 재료 마스터의 적정수량 사용
        //  - 둘 다 null이면 thresholdExpr도 null → where(inv.quantity < threshold) 가 false로 빠져나감(안잡힘)
        NumberExpression<BigDecimal> thresholdExpr =
                Expressions.numberTemplate(BigDecimal.class,
                        "COALESCE({0}, {1})", inv.optimalQuantity, m.optimalQuantity);

        // ratio := qty / NULLIF(threshold, 0)
        //  - threshold가 0이면 NULL → ratio가 null → 정렬상 뒤쪽(실무에선 threshold 0은 비정상 데이터로 간주)
        NumberExpression<Double> ratioExpr =
                Expressions.numberTemplate(Double.class,
                        "({0} / NULLIF({1}, 0))", inv.quantity, thresholdExpr);

        return query
                .select(Projections.constructor(HqStockLowCandidate.class,
                        m.id,                 // materialId
                        m.name,               // materialName
                        inv.quantity,         // qty(현재 본사 재고수량)
                        thresholdExpr         // threshold(임계/적정 수량)
                ))
                .from(inv)
                .join(inv.material, m)
                .where(
                        // 사용중인 재료만
                        m.materialStatus.eq(MaterialStatus.USE),
                        // ★ 핵심: '부족' 판단식
                        inv.quantity.lt(thresholdExpr)
                )
                // 부족 비율이 낮은 것부터(가장 위험) + 순서보조
                .orderBy(ratioExpr.asc(), inv.quantity.asc())
                .limit(Math.max(1, maxRows))
                // 퍼포먼스 힌트(조회 전용)
                .setHint("org.hibernate.readOnly", true)
                .setHint("org.hibernate.flushMode", "COMMIT")
                .setHint("jakarta.persistence.query.timeout", 3000)
                .fetch();
    }

    /**
     * =========================================
     * 추후에 lot 나오면 변경 예정!!!!!!!!!!!!!!!!!!
     * =========================================
     * (2) 유통기한 임박 판단 로직
     *  - LOT 단위 테이블(QInventoryBatch)을 기준으로, 본사 보유분만 조회.
     *  - daysLeft := DATEDIFF(expireDate, today)
     *  - 기간 필터(닫힌–열린):
     *      expireDate >= today  AND  expireDate < today + (daysThreshold + 1)
     *    → today(0일 남음)부터 N일 남음까지 포함 (예: N=2면 0,1,2 포함)
     *  - 수량이 0보다 큰 LOT만 의미 있음.
     *  - HQ 소유 기준:
     *      여기서는 b.store.isNull()을 "HQ 보유"로 간주(모델에 따라 HQ 전용 storeId가 있으면 eq(HQ_ID)로 교체)
     */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<HqExpireSoonCandidate> findExpireSoon(LocalDate today, int daysThreshold, int maxRows) {

        QInventoryBatch b = QInventoryBatch.inventoryBatch; // LOT/입고 배치 테이블 가정
        QMaterial m       = QMaterial.material;

        // [닫힌-열린] 범위의 '상한' (today부터 N일까지 포함하려고 +1)
        //  ex) today=10일, N=2 → [10, 13) → 10, 11, 12일까지 만료되는 LOT 포함
        LocalDate endExclusive = today.plusDays(daysThreshold + 1);

        // daysLeft := 만료일까지 남은 일수 (오늘이 0)
        NumberExpression<Integer> daysLeftExpr =
                Expressions.numberTemplate(Integer.class,
                        "DATEDIFF({0}, {1})", b.expirationDate, today);

        return query
                .select(Projections.constructor(HqExpireSoonCandidate.class,
                        m.id,             // materialId
                        m.name,           // materialName
                        b.lotNo,          // lot (동일 재료 여러 LOT 구분)
                        b.expirationDate, // expireDate (DTO에 있으면 사용)
                        daysLeftExpr      // daysLeft (남은 일수)
                ))
                .from(b)
                .join(b.material, m)
                .where(
                        // 사용중인 재료만
                        m.materialStatus.eq(MaterialStatus.USE),

                        // ★ HQ 소유분만: 현재 모델에선 store가 null이면 본사 보유로 간주
                        //    - 만약 HQ도 store 레코드가 있고 FK로 매핑된다면: b.store.id.eq(HQ_STORE_ID) 로 교체
                        b.store.isNull(),

                        // 실제 재고가 남아 있어야 의미가 있음
                        b.quantity.gt(BigDecimal.ZERO),

                        // 오늘 이후 만료 + N일 이내
                        b.expirationDate.goe(today),
                        b.expirationDate.lt(endExclusive) // [today, today+N] 포함 (닫힌–열린)
                )
                // 남은 일수 적은 것(더 급한 것) 우선
                .orderBy(daysLeftExpr.asc(), b.expirationDate.asc())
                .limit(Math.max(1, maxRows))
                // 퍼포먼스 힌트(조회 전용)
                .setHint("org.hibernate.readOnly", true)
                .setHint("org.hibernate.flushMode", "COMMIT")
                .setHint("jakarta.persistence.query.timeout", 3000)
                .fetch();
    }
}
