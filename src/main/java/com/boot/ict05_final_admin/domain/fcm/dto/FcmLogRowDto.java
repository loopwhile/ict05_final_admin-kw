package com.boot.ict05_final_admin.domain.fcm.dto;

import java.time.LocalDateTime;

/**
 * FCM 전송 로그 목록에 사용되는 Row DTO (읽기 전용).
 *
 * @param id              로그 ID
 * @param topic           전송된 토픽(토픽 전송인 경우)
 * @param token           대상 토큰(단일 전송인 경우)
 * @param title           전송된 제목
 * @param body            전송된 본문
 * @param resultMessageId FCM으로부터 반환된 메시지 ID
 * @param resultError     전송 실패 시 에러 메시지(없으면 null)
 * @param sentAt          전송 시각
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public record FcmLogRowDto(
		Long id,
		String topic,
		String token,
		String title,
		String body,
		String resultMessageId,
		String resultError,
		LocalDateTime sentAt
) { }
