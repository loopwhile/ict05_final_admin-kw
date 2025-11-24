package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 배치별 출고 이력 행 DTO.
 *
 * <p>특정 LOT에서 발생한 출고 건의 헤더 레벨 요약 정보.</p>
 *
 * <p>필드 규칙:</p>
 * <ul>
 *   <li>{@code outId}: 출고 헤더 ID</li>
 *   <li>{@code storeId}/{@code storeName}: 가맹점 식별/표시 정보(내부 사용/폐기 등 매장 미지정 케이스는 null 가능)</li>
 *   <li>{@code outDate}: ISO-8601 LocalDateTime</li>
 *   <li>{@code qty}: 출고 수량(DECIMAL(15,3) 스케일 가정)</li>
 *   <li>{@code memo}: 비고</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InventoryOutLotHistoryRowDTO {

    /** 출고 헤더 ID */
    private Long outId;

    /** 가맹점 ID(선택) */
    private Long storeId;

    /** 가맹점명(선택) */
    private String storeName;

    /** 출고 일시(ISO-8601 LocalDateTime) */
    @Schema(type = "string", format = "date-time")
    private LocalDateTime outDate;

    /** 출고 수량(DECIMAL(15,3)) */
    private BigDecimal qty;

    /** 비고 */
    private String memo;
}
