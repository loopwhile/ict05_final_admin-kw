package com.boot.ict05_final_admin.domain.home.dto;

/**
 * 매장별 매출 랭킹/표용 DTO
 */
public record StoreRevenue(
        Long storeId,
        String storeName,
        long revenue
) {
}
