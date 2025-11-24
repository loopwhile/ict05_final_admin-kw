package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.MenuShow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 메뉴 목록 행 DTO.
 *
 * <p>목록 화면/API에서 노출되는 핵심 요약 정보(상태, 카테고리, 가격, 칼로리, 재료명 목록 등)를 보유한다.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "메뉴 목록 요약 DTO")
public class MenuListDTO {

    /** 메뉴 시퀀스 */
    @Schema(description = "메뉴 ID")
    private Long menuId;

    /** 판매 상태 */
    @Schema(description = "판매 상태", implementation = MenuShow.class)
    private MenuShow menuShow;

    /** 메뉴명 */
    @Schema(description = "메뉴명")
    private String menuName;

    /** 메뉴 코드 */
    @Schema(description = "메뉴 코드")
    private String menuCode;

    /** 메뉴 카테고리 ID */
    @Schema(description = "메뉴 카테고리 ID")
    private Long menuCategoryId;

    /** 카테고리명 */
    @Schema(description = "카테고리명")
    private String menuCategoryName;

    /** 주재료 리스트(문자열) */
    @Schema(description = "주재료 이름 목록")
    private List<String> materialNames;

    /** 가격 */
    @Schema(description = "가격", type = "number", format = "decimal")
    private BigDecimal menuPrice;

    /** 칼로리 */
    @Schema(description = "칼로리(kcal)")
    private Integer menuKcal;

    /** 카테고리 라벨 반환 */
    public String getMenuCategoryLabel() {
        return menuCategoryName != null ? menuCategoryName : "";
    }

    /** 재료명 연결 문자열 반환 */
    public String getMaterialNamesJoined() {
        return materialNames != null ? String.join(", ", materialNames) : "";
    }
}
