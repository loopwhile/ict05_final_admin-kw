package com.boot.ict05_final_admin.domain.home.repository;

import com.boot.ict05_final_admin.domain.home.dto.*;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

public interface HomeRepositoryCustom {
    // 1) KPI 요약 (매출, 주문수, 활성매장수 등)
    KpiSummary kpiSummary(LocalDateTime from, LocalDateTime to,
                          @Nullable Set<Long> storeIds);

    // 2) 월간 시계열 (차트)
    List<Point<Long>> salesByMonth(LocalDateTime from, LocalDateTime to,
                                   @Nullable Set<Long> storeIds);

    // 3) 주간 시계열 (차트: 월~일)
    List<Point<Long>> salesByWeek(LocalDateTime weekStartInclusive, // 월 00:00:00
                                  @Nullable Set<Long> storeIds);

    // 4) 물류 매출(월간)
    List<Point<Long>> logisticsByMonth(LocalDateTime from, LocalDateTime to,
                                       @Nullable Set<Long> storeIds);

    // 5) 매장별 매출 랭킹 (표)
    List<StoreRevenue> topStores(LocalDateTime from, LocalDateTime to,
                                 @Nullable Set<Long> storeIds, int limit);

    // 6) 매장별 증감률(전월/전주 대비)
    List<StoreGrowth> storeGrowth(LocalDateTime curFrom, LocalDateTime curTo,
                                  LocalDateTime prevFrom, LocalDateTime prevTo,
                                  @Nullable Set<Long> storeIds, int limit);

    // 7) 범용 시계열(차트 공통화: 일/주/월 그룹핑)
    <T> List<Point<Long>> timeSeries(AggregationTarget target, // SALES, LOGISTICS 등
                                     GroupByPeriod period,     // DAY, WEEK, MONTH
                                     LocalDateTime from, LocalDateTime to,
                                     @Nullable Set<Long> storeIds);

    // 8) 존재 월/주 목록(차트 축 고정용)
    List<YearMonth> existingMonths(LocalDateTime from, LocalDateTime to);
}
