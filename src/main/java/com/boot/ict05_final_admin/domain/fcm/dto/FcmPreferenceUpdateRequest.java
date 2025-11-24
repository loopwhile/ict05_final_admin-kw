package com.boot.ict05_final_admin.domain.fcm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * HQ 사용자가 자신의 FCM 수신 설정을 변경할 때 사용하는 요청 DTO.
 *
 * @param catNotice         알림(공지) 수신 여부
 * @param catStockLow       재고 부족 카테고리 수신 여부
 * @param catExpireSoon     유통기한 임박 카테고리 수신 여부
 * @param thresholdDays     임박 기준 일수 (1~30)
 * @param applySubscriptions true일 경우 변경 즉시 토픽 구독/해제 반영
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public record FcmPreferenceUpdateRequest(
		Boolean catNotice,
		Boolean catStockLow,
		Boolean catExpireSoon,
		@Min(1) @Max(30) Integer thresholdDays,
		Boolean applySubscriptions
) { }
