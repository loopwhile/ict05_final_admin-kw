package com.boot.ict05_final_admin.domain.home.dto;

import java.util.List;

public record DashboardViewDTO(
        // KPI
        long kpiRevenueThisMonth,
        int  kpiActiveStores,
        long kpiOrderCount,
        int  kpiNewStores,
        double kpiRevenueGrowthPct,

        // 차트들
        List<String> salesLabels,  List<Long> salesValues,     // 월별 매출
        List<String> weeklyLabels, List<Long> weeklyValues,    // 주간 매출
        List<String> logiLabels,   List<Long> logiValues,      // 물류 매출

        // 테이블
        List<StoreRow> storeRows
) {
    public record StoreRow(String store, long revenue, double growth) {}
}
