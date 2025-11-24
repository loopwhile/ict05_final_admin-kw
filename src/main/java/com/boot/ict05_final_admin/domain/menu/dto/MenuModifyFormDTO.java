package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.MenuShow;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 메뉴 수정 폼 DTO.
 *
 * <p>메뉴의 기본 정보, 표시 상태, 카테고리 및 레시피 구성을 수정할 때 사용한다.</p>
 */
@Data
@Builder
@Schema(description = "메뉴 수정 폼 DTO")
public class MenuModifyFormDTO {

    /** 수정 대상 메뉴 ID */
    @Schema(description = "수정 대상 메뉴 ID")
    private Long menuId;

    /** 메뉴 카테고리 ID (필요시 함께 전달) */
    @Schema(description = "메뉴 카테고리 ID")
    private Long menuCategoryId;

    /** 카테고리 */
    @Schema(description = "카테고리명")
    private String menuCategory;

    /** 판매 상태 */
    @Schema(description = "판매 상태", implementation = MenuShow.class)
    private MenuShow menuShow;

    /** 메뉴코드 */
    @Schema(description = "메뉴 코드")
    private String menuCode;

    /** 메뉴명 */
    @Schema(description = "메뉴명")
    private String menuName;

    /** 영문명  */
    @Schema(description = "영문명")
    private String menuNameEnglish;

    /** 가격 */
    @DecimalMin("0.0")
    @Schema(description = "가격")
    private BigDecimal menuPrice;

    /** 설명 */
    @Schema(description = "메뉴 설명")
    private String menuInformation;

    /** 칼로리(kcal) */
    @Schema(description = "칼로리(kcal)")
    private Integer menuKcal;

    /** 재료구성 – 주재료 */
    @Schema(description = "주재료 구성 목록")
    private List<RecipeItemDTO> mainMaterials;

    /** 재료구성 – 소스 */
    @Schema(description = "소스 구성 목록")
    private List<RecipeItemDTO> sauceMaterials;
}
