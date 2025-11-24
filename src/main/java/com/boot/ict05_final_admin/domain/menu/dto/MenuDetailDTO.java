package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.MenuCategory;
import com.boot.ict05_final_admin.domain.menu.entity.MenuShow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 메뉴 상세 정보 DTO.
 *
 * <p>카테고리, 표시 상태, 가격/칼로리, 설명 및 레시피 구성(주재료/소스)을 포함한다.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "메뉴 상세 정보")
public class MenuDetailDTO {

    /** 메뉴 고유 ID */
    @Schema(description = "메뉴 ID")
    private Long menuId;

    /** 메뉴 카테고리 ID */
    @Schema(description = "메뉴 카테고리 ID")
    private Long menuCategoryId;

    /** 메뉴 카테고리 엔티티 */
    @Schema(description = "메뉴 카테고리 엔티티")
    private MenuCategory menuCategory;

    /** 메뉴 카테고리명 */
    @Schema(description = "메뉴 카테고리명")
    private String menuCategoryName;

    /** 판매 상태 */
    @Schema(description = "판매 상태", implementation = MenuShow.class)
    private MenuShow menuShow;

    /** 메뉴 코드 */
    @Schema(description = "메뉴 코드")
    private String menuCode;

    /** 메뉴명 */
    @Schema(description = "메뉴명")
    private String menuName;

    /** 영문명 */
    @Schema(description = "영문명")
    private String menuNameEnglish;

    /** 가격 */
    @Schema(description = "가격", type = "number", format = "decimal")
    private BigDecimal menuPrice;

    /** 칼로리 */
    @Schema(description = "칼로리(kcal)")
    private Integer menuKcal;

    /** 설명 */
    @Schema(description = "메뉴 설명")
    private String menuInformation;

    /** 주재료 구성 */
    @Schema(description = "주재료 구성 목록", implementation = RecipeItemDTO.class)
    private List<RecipeItemDTO> mainMaterials;

    /** 소스 구성 */
    @Schema(description = "소스 구성 목록", implementation = RecipeItemDTO.class)
    private List<RecipeItemDTO> sauceMaterials;
}
