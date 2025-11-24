package com.boot.ict05_final_admin.domain.home.service;

import com.boot.ict05_final_admin.domain.home.dto.*;
import com.boot.ict05_final_admin.domain.home.repository.HomeRepositoryCustom;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 대시보드 화면에 필요한 집계 데이터를 조립하는 서비스
 *
 * 역할
 * 1 이번 달 KPI 요약 수치 조회
 * 2 최근 6개월 월별 매출 시계열 데이터 생성
 * 3 이번 주 월요일 기준 주간 매출 시계열 데이터 생성
 * 4 최근 6개월 물류 매출 시계열 데이터 생성
 * 5 이번 달 매장별 매출 상위 목록과 전월 대비 증감률 병합
 *
 * 기간 기준
 * - KPI      이번 달 시작 00 00 00 부터 다음 달 시작 00 00 00 직전까지
 * - 월간 차트 최근 6개월 범위의 월 시작부터 다음 달 시작까지
 * - 주간 차트 이번 주 월요일 00 00 00 기준 7일
 * - 물류 차트 월간 차트와 동일
 *
 * 주의 사항
 * - 매장 필터가 없으면 storeFilter 는 null 로 전달
 * - 저장소가 null을 반환할 수 있는 값은 0으로 치환하여 뷰 단에서 안정적으로 사용
 * - 상위 매장 표는 이번 달 매출 상위 topLimit 과 전월 대비 성장률을 id 기준으로 조인하여 구성
 */
@Service
@RequiredArgsConstructor
public class HomeService {

    private final HomeRepositoryCustom homeRepository;

    /**
     * 대시보드 뷰 데이터 조립
     * - 기간 기준:
     *   * KPI: 이번 달 시작 ~ 다음 달 시작
     *   * 월간 차트: 최근 6개월(이번 달 포함) 시작 ~ 다음 달 시작
     *   * 주간 차트: 이번 주 월요일 00:00 ~ 일요일 24:00 (Repository는 주간 집계 메서드 사용)
     *   * 물류 매출: 월간 차트와 동일한 범위
     */
    public DashboardViewDTO buildDashboard() {


        LocalDateTime now = LocalDateTime.now();

        // 이번 달
        LocalDateTime monthStart = YearMonth.from(now).atDay(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);

        // 최근 6개월(이번 달 포함) → monthStart 기준으로 5개월 전부터
        LocalDateTime sixMonthsStart = monthStart.minusMonths(5);

        // 이번 주(월 ~ 일) 시작
        LocalDate weekMonday = now.toLocalDate().with(DayOfWeek.MONDAY);
        LocalDateTime weekStart = weekMonday.atStartOfDay();

        // 필터(매장 선택 없으면 null)
        @Nullable Set<Long> storeFilter = null;

        // 1) KPI
        KpiSummary kpi = homeRepository.kpiSummary(monthStart, nextMonthStart, storeFilter);

        long kpiRevenueThisMonth   = kpi != null ? kpi.revenueThisMonth()   : 0L;
        int  kpiActiveStores       = kpi != null ? kpi.activeStores()       : 0;
        long kpiOrderCount         = kpi != null ? kpi.orderCount()         : 0L;
        int  kpiNewStores          = kpi != null ? kpi.newStores()          : 0;

        LocalDateTime nowFloor = now.truncatedTo(ChronoUnit.MINUTES);

        // 이번 달 누적: [월초, 현재분 + 1분)
        LocalDateTime curFrom = monthStart;
        LocalDateTime curTo   = nowFloor.plusMinutes(1);
        long curMtd = homeRepository.kpiSummary(curFrom, curTo, storeFilter).revenueThisMonth();

        // 전월 동일 ‘분’ 시각 만들기 (말일 보정)
        YearMonth prevYm = YearMonth.from(now).minusMonths(1);
        LocalDateTime prevStart = prevYm.atDay(1).atStartOfDay();
        LocalDateTime prevEndExclusive = prevYm.plusMonths(1).atDay(1).atStartOfDay();

        int prevDom = Math.min(now.getDayOfMonth(), prevYm.lengthOfMonth());
        LocalDateTime prevSameMinute = prevYm.atDay(prevDom)
                .atTime(now.getHour(), now.getMinute());

        // 전월 누적: [전월초, min(전월 동일분+1분, 전월말))  (반개구간)
        LocalDateTime prevTo = prevSameMinute.plusMinutes(1);
        if (!prevTo.isBefore(prevEndExclusive)) prevTo = prevEndExclusive;

        long prevMtd = homeRepository.kpiSummary(prevStart, prevTo, storeFilter).revenueThisMonth();

        // 성장률 (분모 0 보정만)
        double kpiRevenueGrowthPct = (prevMtd > 0)
                ? ((curMtd - prevMtd) * 100.0 / prevMtd)
                : (curMtd > 0 ? 100.0 : 0.0);

        // 2) 월별 매출(최근 6개월)
        List<Point<Long>> salesMonthPoints =
                homeRepository.salesByMonth(sixMonthsStart, nextMonthStart, storeFilter);

        // 라벨은 "n월"로, 값은 long
        List<String> salesLabels = salesMonthPoints.stream()
                .map(p -> p.at().getMonthValue() + "월")
                .toList();
        List<Long> salesValues = salesMonthPoints.stream()
                .map(Point::value)
                .map(v -> v == null ? 0L : v)
                .toList();

        // 3) 주간 매출(이번 주)
        List<Point<Long>> weeklyPoints =
                homeRepository.salesByWeek(weekStart, storeFilter);

        // 라벨: 월~일
        final String[] WEEK_KO = { "월","화","수","목","금","토","일" };
        List<String> weeklyLabels = new ArrayList<>();
        List<Long> weeklyValues = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weeklyLabels.add(WEEK_KO[i]);
            long val = 0L;
            if (i < weeklyPoints.size() && weeklyPoints.get(i) != null && weeklyPoints.get(i).value() != null) {
                val = weeklyPoints.get(i).value();
            }
            weeklyValues.add(val);
        }

        // 4) 물류 매출(월간, 최근 6개월)
        List<Point<Long>> logiMonthPoints =
                homeRepository.logisticsByMonth(sixMonthsStart, nextMonthStart, storeFilter);
        List<String> logiLabels = logiMonthPoints.stream()
                .map(p -> p.at().getMonthValue() + "월")
                .toList();
        List<Long> logiValues = logiMonthPoints.stream()
                .map(Point::value)
                .map(v -> v == null ? 0L : v)
                .toList();

        // 5) 매장별 매출 랭킹(이번 달) + 증감률(전월 대비)
        int topLimit = 10;

        // 이번 달 상위 매장
        List<StoreRevenue> topStores =
                homeRepository.topStores(monthStart, nextMonthStart, storeFilter, topLimit);

        // 전월 기간
        LocalDateTime prevMonthStart = monthStart.minusMonths(1);
        LocalDateTime prevMonthEnd   = monthStart;

        // 전월 대비 증감률
        List<StoreGrowth> growthRows =
                homeRepository.storeGrowth(monthStart, nextMonthStart,
                        prevMonthStart, prevMonthEnd,
                        storeFilter, topLimit);

        // growth를 id→pct 로 매핑
        Map<Long, Double> growthPctById = growthRows.stream()
                .collect(Collectors.toMap(StoreGrowth::storeId, StoreGrowth::growthPct, (a, b) -> a));

        // 화면 테이블 DTO로 변환
        List<DashboardViewDTO.StoreRow> storeRows = topStores.stream()
                .map(r -> new DashboardViewDTO.StoreRow(
                        r.storeName(),                                 // 매장명
                        r.revenue(),                               // 매출
                        growthPctById.getOrDefault(r.storeId(), 0.0) // 증감률(%)
                ))
                .toList();
        // 최종 조립
        return new DashboardViewDTO(
                // KPI
                kpiRevenueThisMonth,
                kpiActiveStores,
                kpiOrderCount,
                kpiNewStores,
                kpiRevenueGrowthPct,

                // 월별 매출
                salesLabels, salesValues,

                // 주간 매출
                weeklyLabels, weeklyValues,

                // 물류 매출
                logiLabels, logiValues,

                // 표
                storeRows
        );
    }

}
