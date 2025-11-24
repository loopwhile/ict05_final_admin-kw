package com.boot.ict05_final_admin.domain.fcm.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * 유통기한 임박 후보 항목 DTO (알림 생성 전 후보 데이터).
 *
 * <p>서비스 레이어에서 스캔 결과를 이 DTO로 수집한 후 알림 템플릿에 매핑한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HqExpireSoonCandidate {
    private Long materialId;
    private String materialName;
    private String lot;
    private LocalDate expireDate;
    private Integer daysLeft;
}
