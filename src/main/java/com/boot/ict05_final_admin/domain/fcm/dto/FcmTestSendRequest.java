package com.boot.ict05_final_admin.domain.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 테스트 전송 요청 DTO (토픽 또는 토큰 대상).
 *
 * @param tokenOrTopic 대상 토큰 또는 토픽 문자열 (필수)
 * @param topic        true이면 토픽 전송, false이면 단일 토큰 전송
 * @param title        전송할 제목 (필수)
 * @param body         전송할 본문 (필수)
 * @param data         전송할 추가 데이터 맵 (옵션)
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public record FcmTestSendRequest(
        @NotBlank String tokenOrTopic,
        boolean topic,
        @NotBlank String title,
        @NotBlank String body,
        Map<String, String> data
) { }
