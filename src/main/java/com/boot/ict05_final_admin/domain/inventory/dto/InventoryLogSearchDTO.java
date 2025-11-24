package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

/**
 * 재고 로그 검색 조건 DTO.
 *
 * <p>
 * /admin/inventory/log/{materialId} 화면의 검색 폼(type, 기간)을
 * Repository 계층으로 전달하기 위한 전용 DTO.
 * </p>
 *
 * <p>필터 규칙:</p>
 * <ul>
 *   <li>{@code materialId}: 필수(경로 변수)</li>
 *   <li>{@code type}: INCOME / OUTGO / ADJUST / null(전체)</li>
 *   <li>{@code startDate}/{@code endDate}: ISO-8601(LocalDate), 둘 다 지정 시 {@code startDate ≤ endDate}</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "InventoryLogSearchDTO", description = "재고 로그 검색 조건 DTO")
public class InventoryLogSearchDTO {

    /** 재료 ID(경로 변수) */
    @Schema(description = "재료 ID", example = "65", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long materialId;

    /** 로그 유형 필터(INCOME / OUTGO / ADJUST), null이면 전체 */
    @Schema(description = "로그 유형 필터(INCOME / OUTGO / ADJUST)", example = "OUTGO")
    private String type;

    /** 시작일(포함, ISO-8601) */
    @Schema(description = "검색 시작일(포함)", example = "2025-11-01")
    private LocalDate startDate;

    /** 종료일(포함, ISO-8601) */
    @Schema(description = "검색 종료일(포함)", example = "2025-11-14")
    private LocalDate endDate;
}
