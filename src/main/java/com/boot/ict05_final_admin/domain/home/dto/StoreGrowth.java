package com.boot.ict05_final_admin.domain.home.dto;


/**
 * 매장별 증감률 (현재기간 vs 이전기간)
 */
public record StoreGrowth(
        Long storeId,
        String storeName,
        long currentRevenue,
        long previousRevenue,
        double growthPct  // ((current - previous) / previous) * 100
) {
}
