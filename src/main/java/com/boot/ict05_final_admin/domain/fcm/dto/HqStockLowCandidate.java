package com.boot.ict05_final_admin.domain.fcm.dto;

import lombok.*;

/**
 * 재고 부족 후보 항목 DTO (알림 생성 전 후보 데이터).
 *
 * <p>서비스 레이어에서 재고 부족 항목을 이 DTO로 수집한 뒤 알림을 생성한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HqStockLowCandidate {
    private Long materialId;
    private String materialName;
    private Long qty;
    private Long threshold;
}
