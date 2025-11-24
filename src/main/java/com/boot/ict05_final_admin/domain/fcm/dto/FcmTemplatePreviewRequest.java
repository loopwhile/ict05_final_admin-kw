package com.boot.ict05_final_admin.domain.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * FCM 템플릿 미리보기 요청 DTO.
 *
 * @param templateCode 템플릿 코드(필수)
 * @param variables    템플릿 렌더링에 사용될 변수 맵
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public record FcmTemplatePreviewRequest(
        @NotBlank String templateCode,
        Map<String, Object> variables
) { }
