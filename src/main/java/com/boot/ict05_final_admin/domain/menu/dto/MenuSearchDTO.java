package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.MenuShow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 메뉴 검색/필터 조건을 표현하는 DTO.
 *
 * <p>검색어, 검색 타입, 페이지 크기, 판매 상태, 카테고리 필터 등을 포함한다.</p>
 */
@Data
@Schema(description = "메뉴 검색/필터 파라미터")
public class MenuSearchDTO {

    /** 검색어 */
    @Schema(description = "검색어")
    private String s;

    /** 검색 타입 */
    @Schema(description = "검색 타입(name|info|all)")
    private String type;

    /** 페이지 사이즈 */
    @Schema(description = "페이지 사이즈(문자열)")
    private String size = "10";

    /** 판매 상태 */
    @Schema(description = "판매 상태", implementation = MenuShow.class)
    private MenuShow menuShow;

    /** 카테고리 ID */
    @Schema(description = "카테고리 ID")
    private Long menuCategoryId;

    /** 카테고리명(표시용) */
    @Schema(description = "카테고리명(표시용)")
    private String menuCategoryName;
}
