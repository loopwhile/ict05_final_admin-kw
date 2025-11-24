package com.boot.ict05_final_admin.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 직원 간단 정보 DTO.
 *
 * <p>
 * 직원(점주 포함)의 ID와 이름을 반환하거나 조회할 때 사용된다.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "직원 ID/이름 조회 DTO")
public class StaffNameDTO {

    /** 직원 고유 ID */
    @Schema(description = "직원 ID", example = "3001")
    private Long staffId;

    /** 직원 이름 */
    @Schema(description = "직원 이름", example = "홍길동")
    private String staffName;
}
