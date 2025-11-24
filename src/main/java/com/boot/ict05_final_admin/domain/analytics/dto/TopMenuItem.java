package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * 상위 메뉴 아이템 DTO.
 *
 * <p>메뉴별 누적 수량/매출 및 전체 대비 비중(%)을 포함한다.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopMenuItem {

    /** 메뉴 ID */
    private Long menuId;

    /** 메뉴명 */
    private String menuName;

    /** 누적 판매수량 */
    private Long quantity;

    /** 누적 매출 */
    private BigDecimal sales;

    /** 전체 라인매출 대비 비중(%) */
    private BigDecimal ratio;
}
