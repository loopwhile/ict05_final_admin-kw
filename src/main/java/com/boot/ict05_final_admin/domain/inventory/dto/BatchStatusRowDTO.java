package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * HQ 배치(LOT) 현황 DTO.
 *
 * <p>본사 재고의 배치(LOT) 단위 상태를 표현한다.</p>
 *
 * <p>필드 설명/규칙:</p>
 * <ul>
 *   <li>{@code batchId}: 배치 고유 ID</li>
 *   <li>{@code lotNo}: LOT 번호(예: {@code LOT251107-003})</li>
 *   <li>{@code receivedDate}: 입고 일시(ISO-8601)</li>
 *   <li>{@code expirationDate}: 유통기한(일 단위, ISO-8601)</li>
 *   <li>{@code receivedQty}: 입고 수량(DECIMAL(15,3) 스케일 준수)</li>
 *   <li>{@code remainQty}: 현재 잔량(DECIMAL(15,3) 스케일 준수)</li>
 *   <li>{@code unitPrice}: 입고 단가(매입가, 금액 단위는 시스템 공통 정책 따름)</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BatchStatusRowDTO {

    /** 배치 고유 ID */
    private Long batchId;

    /** LOT 번호(예: LOT251107-003) */
    private String lotNo;

    /** 입고 일시 */
    @Schema(type = "string", format = "date-time")
    private LocalDateTime receivedDate;

    /** 유통기한(일 단위) */
    @Schema(type = "string", format = "date")
    private LocalDate expirationDate;

    /** 입고 수량(DECIMAL(15,3)) */
    private BigDecimal receivedQty;

    /** 현재 잔량(DECIMAL(15,3)) */
    private BigDecimal remainQty;

    /** 입고 단가(매입가) */
    private BigDecimal unitPrice;
}
