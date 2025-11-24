package com.boot.ict05_final_admin.domain.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 재료 간략 정보 DTO.
 *
 * <p>재료 선택 목록, 레시피 편집 등에서 식별자와 이름만 필요할 때 사용한다.</p>
 */
@Getter
@AllArgsConstructor
@Schema(description = "재료 간략 정보 DTO")
public class MaterialSimpleDTO {

    /** 재료 식별자 */
    @Schema(description = "재료 ID")
    private Long materialId;

    /** 재료 이름 */
    @Schema(description = "재료명")
    private String materialName;
}
