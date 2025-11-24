package com.boot.ict05_final_admin.domain.home.dto;

import java.time.LocalDateTime;

/**
 * 시계열/카테고리 공통 포인트
 * - label: 차트 라벨(예: "1월", "월", "2025-01-01")
 * - at   : 버킷 시작 시각(LocalDateTime, DAY/WEEK/MONTH 버킷의 기준)
 * - value: 값
 */
public record Point<T>(
        String label,
        LocalDateTime at,
        T value
) {
}
