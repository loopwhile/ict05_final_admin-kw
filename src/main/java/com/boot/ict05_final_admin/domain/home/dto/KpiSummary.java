package com.boot.ict05_final_admin.domain.home.dto;

/**
 * KPI 집계 결과 (기간/필터 반영)
 */
public record KpiSummary (
    long revenueThisMonth,
    int  activeStores,
    long orderCount,
    int  newStores,
    double revenueGrowthPct  // 전월 대비 등, 서비스에서 계산해 채워도 됨
) {}
