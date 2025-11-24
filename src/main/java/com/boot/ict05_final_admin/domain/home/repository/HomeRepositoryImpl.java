package com.boot.ict05_final_admin.domain.home.repository;

import com.boot.ict05_final_admin.domain.home.dto.*;
import com.boot.ict05_final_admin.domain.order.entity.QCustomerOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.QReceiveOrder;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.*;

import static com.boot.ict05_final_admin.domain.receiveOrder.entity.QReceiveOrder.receiveOrder;
import static com.querydsl.core.types.dsl.Expressions.stringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class HomeRepositoryImpl implements HomeRepositoryCustom{

    private final JPAQueryFactory QueryFactory;

    private static final QStore store = QStore.store;
    private static final QCustomerOrder customerOrder = QCustomerOrder.customerOrder;
    private static final QReceiveOrder receiveOrder = QReceiveOrder.receiveOrder;

    // 기간 필터 (주문: LocalDateTime)
    private BooleanExpression range(DateTimePath<LocalDateTime> dt, LocalDateTime from, LocalDateTime to) {
        return dt.goe(from).and(dt.lt(to));
    }

    // 기간 필터 (물류: LocalDate)
    private BooleanExpression range(DatePath<LocalDate> d, LocalDate from, LocalDate to) {
        return d.goe(from).and(d.lt(to));
    }

    private BooleanExpression storeFilter(@Nullable Set<Long> storeIds, NumberPath<Long> storeIdPath) {
        return (storeIds == null || storeIds.isEmpty()) ? null : storeIdPath.in(storeIds);
    }

    // yyyy-MM (ex: 2025-01)
    private StringExpression monthKey(Expression<?> dt) {
        return stringTemplate("DATE_FORMAT({0}, '%Y-%m')", dt);
    }

    // yyyy-MM-dd
    private StringExpression ymdKey(Expression<?> dt) {
        return stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", dt);
    }

    // ISO 주차 라벨(예: 2025-01) → MariaDB: %x-%v
    private StringExpression isoWeekKey(Expression<?> dt) {
        return stringTemplate("DATE_FORMAT({0}, '%x-%v')", dt);
    }

    // 라벨→Point 변환 (월)
    private Point<Long> toMonthPoint(String keyYm, Long value) {
        LocalDateTime at = YearMonth.parse(keyYm).atDay(1).atStartOfDay();
        return new Point<>(keyYm, at, value == null ? 0L : value);
    }

    // 라벨→Point 변환 (일)
    private Point<Long> toDayPoint(String keyYmd, Long value) {
        LocalDate d = LocalDate.parse(keyYmd);
        return new Point<>(keyYmd, d.atStartOfDay(), value == null ? 0L : value);
    }

    @Override
    public KpiSummary kpiSummary(LocalDateTime from, LocalDateTime to, @Nullable Set<Long> storeIds) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thisMonthStart = YearMonth.from(now).atDay(1).atStartOfDay();
        LocalDateTime prevMonthStart = thisMonthStart.minusMonths(1);

        // 이번달 MTD
        LocalDateTime curFrom = thisMonthStart;
        LocalDateTime curTo   = now;

        // 전월 동기간 MTD
        Duration elapsed = Duration.between(curFrom, curTo);

        // 전월 끝 경계(배타) = 이번달 1일 00:00
        LocalDateTime prevEndExclusive = thisMonthStart;

        LocalDateTime prevTo = prevMonthStart.plus(elapsed);
        if (prevTo.isAfter(prevEndExclusive)) {
            prevTo = prevEndExclusive; // 31 vs 30 vs 28/29 모두 여기서 자동 보정
        }

        // 이제 KPI는 아래 구간으로 합계/건수/매장수 계산
        // 현재:  [curFrom,  curTo)
        // 전월:  [prevMonthStart, prevTo)

        // 현재기간(이번달 MTD)
        var curSum = QueryFactory
                .select(customerOrder.totalPrice.sum())
                .from(customerOrder)
                .where(
                        range(customerOrder.orderedAt, from, to),
                        storeFilter(storeIds, customerOrder.store.id)
                ).fetchOne();

        // 전월 동기간(MTD)
        var prevSum = QueryFactory
                .select(customerOrder.totalPrice.sum())
                .from(customerOrder)
                .where(
                        range(customerOrder.orderedAt, prevMonthStart, prevTo),
                        storeFilter(storeIds, customerOrder.store.id)
                ).fetchOne();

        Long curOrderCnt = QueryFactory
                .select(customerOrder.id.count())
                .from(customerOrder)
                .where(
                        range(customerOrder.orderedAt, from, to),
                        storeFilter(storeIds, customerOrder.store.id)
                ).fetchOne();

        Long activeStores = QueryFactory
                .select(customerOrder.store.id.countDistinct())
                .from(customerOrder)
                .where(
                        range(customerOrder.orderedAt, from, to),
                        storeFilter(storeIds, customerOrder.store.id)
                ).fetchOne();

        long curRevenue  = curSum  == null ? 0L : curSum.longValue();
        long prevRevenue = prevSum == null ? 0L : prevSum.longValue();

        double growthPct;
        if (prevRevenue > 0) {
            growthPct = ((curRevenue - prevRevenue) * 100.0 / prevRevenue);
        } else {
            // 0 대비는 0%로 표시(원하면 100% 처리로 바꿔도 됨)
            growthPct = 0.0;
        }

        return new KpiSummary(
                curRevenue,
                activeStores == null ? 0 : activeStores.intValue(),
                curOrderCnt == null ? 0L : curOrderCnt,
                0,              // 신규 매장 수는 별도 로직 있으면 채워
                growthPct
        );
    }


    @Override
    public List<Point<Long>> salesByMonth(LocalDateTime from, LocalDateTime to, @Nullable Set<Long> storeIds) {
        StringExpression bucket = monthKey(customerOrder.orderedAt);
        NumberExpression<java.math.BigDecimal> sumExpr = customerOrder.totalPrice.sum();

        List<Tuple> rows = QueryFactory
                .select(bucket, sumExpr)
                .from(customerOrder)
                .where(
                        range(customerOrder.orderedAt, from, to),
                        storeFilter(storeIds, customerOrder.store.id)
                )
                .groupBy(bucket)
                .orderBy(bucket.asc())
                .fetch();

        return rows.stream()
                .map(t -> {
                    String key = t.get(bucket);
                    java.math.BigDecimal v = t.get(sumExpr);
                    return toMonthPoint(key, v == null ? 0L : v.longValue());
                })
                .toList();
    }

    @Override
    public List<Point<Long>> salesByWeek(LocalDateTime weekStartInclusive, @Nullable Set<Long> storeIds) {
        LocalDateTime from = weekStartInclusive;
        LocalDateTime to   = weekStartInclusive.plusDays(7);

        StringExpression bucket = ymdKey(customerOrder.orderedAt);
        NumberExpression<java.math.BigDecimal> sumExpr = customerOrder.totalPrice.sum();

        List<Tuple> rows = QueryFactory
                .select(bucket, sumExpr)
                .from(customerOrder)
                .where(
                        range(customerOrder.orderedAt, from, to),
                        storeFilter(storeIds, customerOrder.store.id)
                )
                .groupBy(bucket)
                .orderBy(bucket.asc())
                .fetch();

        return rows.stream()
                .map(t -> {
                    String key = t.get(bucket); // yyyy-MM-dd
                    java.math.BigDecimal v = t.get(sumExpr);
                    return toDayPoint(key, v == null ? 0L : v.longValue());
                })
                .toList();
    }

    @Override
    public List<Point<Long>> logisticsByMonth(LocalDateTime from, LocalDateTime to, @Nullable Set<Long> storeIds) {
        LocalDate dFrom = from.toLocalDate();
        LocalDate dTo   = to.toLocalDate();

        StringExpression bucket = monthKey(receiveOrder.orderDate);
        NumberExpression<java.math.BigDecimal> sumExpr = receiveOrder.totalPrice.sum();

        List<Tuple> rows = QueryFactory
                .select(bucket, sumExpr)
                .from(receiveOrder)
                .where(
                        range(receiveOrder.orderDate, dFrom, dTo),
                        storeFilter(storeIds, receiveOrder.store.id)
                )
                .groupBy(bucket)
                .orderBy(bucket.asc())
                .fetch();

        return rows.stream()
                .map(t -> {
                    String key = t.get(bucket);
                    java.math.BigDecimal v = t.get(sumExpr);
                    return toMonthPoint(key, v == null ? 0L : v.longValue());
                })
                .toList();
    }

    @Override
    public List<StoreRevenue> topStores(LocalDateTime from, LocalDateTime to,
                                        @Nullable Set<Long> storeIds, int limit) {

        NumberExpression<java.math.BigDecimal> sumExpr = customerOrder.totalPrice.sum();

        List<Tuple> rows = QueryFactory
                .select(
                        customerOrder.store.id,      // Long
                        customerOrder.store.name,    // String
                        sumExpr                          // BigDecimal sum
                )
                .from(customerOrder)
                .where(
                        range(customerOrder.orderedAt, from, to),
                        storeFilter(storeIds, customerOrder.store.id)
                )
                .groupBy(customerOrder.store.id, customerOrder.store.name)
                .orderBy(sumExpr.desc())
                .limit(limit)
                .fetch();

        return rows.stream()
                .map(t -> new StoreRevenue(
                        t.get(customerOrder.store.id),
                        t.get(customerOrder.store.name),
                        (t.get(sumExpr) == null ? 0L : t.get(sumExpr).longValue())
                ))
                .toList();
    }


    @Override
    public List<StoreGrowth> storeGrowth(LocalDateTime curFrom, LocalDateTime curTo,
                                         LocalDateTime prevFrom, LocalDateTime prevTo,
                                         @Nullable Set<Long> storeIds, int limit) {
        NumberExpression<java.math.BigDecimal> sumExpr = customerOrder.totalPrice.sum();

        // 내부 합계 보관용
        record Agg(String name, long sum) {}

        // 현재 기간 합계 (storeId, storeName, sum)
        List<Tuple> curRows = QueryFactory
                .select(
                        customerOrder.store.id,
                        customerOrder.store.name,
                        sumExpr
                )
                .from(customerOrder)
                .where(
                        range(customerOrder.orderedAt, curFrom, curTo),
                        storeFilter(storeIds, customerOrder.store.id)
                )
                .groupBy(customerOrder.store.id, customerOrder.store.name)
                .fetch();

        Map<Long, Agg> cur = curRows.stream().collect(
                java.util.stream.Collectors.toMap(
                        t -> t.get(customerOrder.store.id),
                        t -> new Agg(
                                t.get(customerOrder.store.name),
                                (t.get(sumExpr) == null ? 0L : t.get(sumExpr).longValue())
                        )
                )
        );

        // 이전 기간 합계 (storeId, sum) — 이름은 현재 쪽에서 사용
        List<Tuple> prevRows = QueryFactory
                .select(
                        customerOrder.store.id,
                        sumExpr
                )
                .from(customerOrder)
                .where(
                        range(customerOrder.orderedAt, prevFrom, prevTo),
                        storeFilter(storeIds, customerOrder.store.id)
                )
                .groupBy(customerOrder.store.id)
                .fetch();

        Map<Long, Long> prev = prevRows.stream().collect(
                java.util.stream.Collectors.toMap(
                        t -> t.get(customerOrder.store.id),
                        t -> (t.get(sumExpr) == null ? 0L : t.get(sumExpr).longValue())
                )
        );

        // 현재 기간에 등장한 매장만 대상으로 증감률 계산
        return cur.entrySet().stream()
                .map(e -> {
                    Long storeId = e.getKey();
                    String name  = e.getValue().name();
                    long curSum  = e.getValue().sum();
                    long prevSum = prev.getOrDefault(storeId, 0L);
                    double pct;
                    if (prevSum > 0) {
                        pct = ((curSum - prevSum) * 100.0 / prevSum);
                    } else {
                        pct = (curSum > 0) ? 100.0 : 0.0; // 또는 Double.NaN/ null 로 두고 화면에서 "—" 처리
                    }
                    return new StoreGrowth(storeId, name, curSum, prevSum, pct);

                })
                .sorted(java.util.Comparator.comparing(StoreGrowth::growthPct).reversed())
                .limit(limit)
                .toList();
    }


    @Override
    public <T> List<Point<Long>> timeSeries(AggregationTarget target, GroupByPeriod period, LocalDateTime from, LocalDateTime to, @Nullable Set<Long> storeIds) {
        switch (target) {
            case SALES -> {
                return switch (period) {
                    case MONTH -> salesByMonth(from, to, storeIds);
                    case WEEK  -> salesByWeek(from, storeIds); // 주간은 from을 주 시작으로 받도록 설계
                    case DAY   -> {
                        StringExpression bucket = ymdKey(customerOrder.orderedAt);
                        NumberExpression<java.math.BigDecimal> sumExpr = customerOrder.totalPrice.sum();
                        List<Tuple> rows = QueryFactory
                                .select(bucket, sumExpr)
                                .from(customerOrder)
                                .where(
                                        range(customerOrder.orderedAt, from, to),
                                        storeFilter(storeIds, customerOrder.store.id)
                                )
                                .groupBy(bucket)
                                .orderBy(bucket.asc())
                                .fetch();
                        yield rows.stream()
                                .map(t -> {
                                    String key = t.get(bucket);
                                    java.math.BigDecimal v = t.get(sumExpr);
                                    return toDayPoint(key, v == null ? 0L : v.longValue());
                                })
                                .toList();
                    }
                };
            }
            case LOGISTICS -> {
                // 월 집계만 우선 지원
                return logisticsByMonth(from, to, storeIds);
            }
            default -> {
                return List.of();
            }
        }

    }

    @Override
    public List<YearMonth> existingMonths(LocalDateTime from, LocalDateTime to) {
        StringExpression bucket = monthKey(customerOrder.orderedAt);

        List<String> keys = QueryFactory
                .select(bucket)
                .from(customerOrder)
                .where(range(customerOrder.orderedAt, from, to))
                .groupBy(bucket)
                .orderBy(bucket.asc())
                .fetch();

        return keys.stream()
                .map(YearMonth::parse) // "yyyy-MM" -> YearMonth
                .toList();
    }
}
