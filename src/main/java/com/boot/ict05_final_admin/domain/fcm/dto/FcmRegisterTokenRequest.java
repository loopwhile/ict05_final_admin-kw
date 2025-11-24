package com.boot.ict05_final_admin.domain.fcm.dto;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.entity.PlatformType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * FCM 토큰 등록(업서트) 요청 DTO.
 *
 * @param appType    애플리케이션 종류 (예: HQ 등)
 * @param platform   플랫폼 종류 (ANDROID / IOS / WEB 등)
 * @param token      디바이스 토큰(필수)
 * @param deviceId   디바이스 식별자(옵션)
 * @param memberIdFk 회원 ID 외래키(서버에서 세션 정보로 오버라이드 가능)
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public record FcmRegisterTokenRequest(
        @NotNull AppType appType,
        @NotNull PlatformType platform,
        @NotBlank String token,
        String deviceId,
        Long memberIdFk
) { }
