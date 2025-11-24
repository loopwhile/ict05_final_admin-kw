package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.MenuShow;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 메뉴 작성 폼 DTO.
 *
 * <p>카테고리, 판매 상태, 기본 정보(이름/코드/가격/칼로리/설명)와
 * 레시피 구성(주재료/소스)을 포함한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "메뉴 작성 폼 DTO")
public class MenuWriteFormDTO {

    /** 카테고리 ID */
    @NotNull(message = "카테고리를 선택해주세요")
    @Schema(description = "카테고리 ID")
    private Long menuCategoryId;

    /** 카테고리 */
    private String menuCategoryName;

    /** 판매 상태 */
    @NotNull(message = "판매상태를 선택해주세요")
    @Schema(description = "판매 상태", implementation = MenuShow.class)
    private MenuShow menuShow;

    /** 메뉴명 */
    @NotBlank
    @Size(max = 100)
    @Schema(description = "메뉴명")
    private String menuName;

    /** 영문명 */
    @NotBlank
    @Size(max = 150)
    @Schema(description = "영문명")
    private String menuNameEnglish;

    /** 메뉴코드 */
    @NotNull
    @Schema(description = "메뉴 코드")
    private String menuCode;

    /** 가격 */
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "가격은 0 이상이어야 합니다")
    @Schema(description = "가격", type = "number", format = "decimal")
    private BigDecimal menuPrice;

    /** 설명 */
    @NotNull(message = "설명을 입력해 주세요.")
    @Schema(description = "메뉴 설명")
    private String menuInformation;

    /** 칼로리(kcal) */
    @NotNull(message = "칼로리를 입력해 주세요.")
    @Min(value = 0, message = "칼로리는 0 이상으로 입력해 주세요.")
    @Schema(description = "칼로리(kcal)")
    private Integer menuKcal;

    /** 재료구성 – 주재료 */
    @Valid
    @Builder.Default
    @Schema(description = "주재료 구성 목록", implementation = RecipeItemDTO.class)
    private List<RecipeItemDTO> mainMaterials = new ArrayList<>();

    /** 재료구성 – 소스 */
    @Valid
    @Builder.Default
    @Schema(description = "소스 구성 목록", implementation = RecipeItemDTO.class)
    private List<RecipeItemDTO> sauceMaterials = new ArrayList<>();
}
