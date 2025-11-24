package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.RecipeUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.*;

import java.math.BigDecimal;

/**
 * 레시피 항목 DTO.
 *
 * <p>메뉴 레시피 구성 요소의 식별자, 재료, 수량, 단위, 정렬 정보와 삭제 플래그를 포함한다.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "레시피 항목 DTO")
public class RecipeItemDTO {

    /** 레시피 항목 ID */
    @Schema(description = "레시피 항목 ID")
    private Long menuRecipeId;

    /** 재료 ID */
    @Schema(description = "재료 ID")
    private Long materialId;

    /** 레시피 항목명 */
    @Schema(description = "레시피 항목명")
    private String itemName;

    /** 사용 수량 */
    @DecimalMin(value = "0.0", inclusive = false, message = "수량은 0보다 커야 합니다")
    @Digits(integer = 8, fraction = 2, message = "수량은 소수 둘째 자리까지 입력 가능합니다.")
    @Schema(description = "사용 수량", type = "number", format = "decimal")
    private BigDecimal recipeQty;

    /** 단위 */
    @Schema(description = "단위", implementation = RecipeUnit.class)
    private RecipeUnit recipeUnit;

    /** 표시 순서 */
    @Schema(description = "표시 순서")
    private Integer recipeSortNo;

    /** 삭제 여부 */
    @Schema(description = "삭제 여부")
    private Boolean deleteFlag;
}
