package com.boot.ict05_final_admin.domain.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 단일 로트의 출고 이력 행 DTO.
 *
 * <p>배치(LOT) 기준 출고 이력 조회 시 각 로트에서 차감된 수량과
 * 출고 메타 정보를 전달한다.</p>
 *
 * <p>필드 규칙:</p>
 * <ul>
 *   <li>{@code qty}: DECIMAL(15,3) 스케일 준수</li>
 *   <li>{@code storeId}: 내부 사용/폐기 등 매장 미지정 케이스는 {@code null}</li>
 *   <li>{@code storeName}: 조인 최적화를 위해 생략 가능(선택)</li>
 *   <li>{@code outDate}: ISO-8601 로컬 날짜시각</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BatchOutRowDTO {

    /** 출고 헤더 ID */
    private Long outId;

    /** 가맹점 ID(내부 사용/폐기 등은 null 가능) */
    private Long storeId;

    /** 가맹점 명칭(선택, 미조인 가능) */
    private String storeName;

    /** 해당 로트에서 차감된 출고 수량(DECIMAL(15,3)) */
    private BigDecimal qty;

    /** 출고 일시(ISO-8601 LocalDateTime) */
    private LocalDateTime outDate;
}
