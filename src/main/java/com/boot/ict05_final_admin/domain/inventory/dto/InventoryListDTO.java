package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 본사 재고 목록 DTO.
 *
 * <p>SSR/JSON 목록 그리드에 표시되는 요약 정보를 담는다.</p>
 *
 * <p>필드 규칙:</p>
 * <ul>
 *   <li>{@code quantity}, {@code optimalQuantity}: DECIMAL(15,3) 스케일 가정</li>
 *   <li>{@code status}: {@link InventoryStatus} (SUFFICIENT/LOW/SHORTAGE 등)</li>
 *   <li>{@code updateDate}: 재고 수량 또는 메타 변경의 최신 시각(서버 로컬/DB 타임존 정책 따름)</li>
 * </ul>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryListDTO {

    /** 재고 ID */
    private Long id;

    /** 재료 ID */
    private Long materialId;

    /** 재료명 */
    private String materialName;

    /** 카테고리명 */
    private String categoryName;

    /** 현재 수량(DECIMAL(15,3)) */
    private BigDecimal quantity;

    /** 적정 수량(DECIMAL(15,3)) */
    private BigDecimal optimalQuantity;

    /** 판매 단위(예: 개, 박스, g, ml 등) */
    private String materialSalesUnit;

    /** 재고 상태(SUFFICIENT/LOW/SHORTAGE) */
    @Setter
    private InventoryStatus status;

    /** 마지막 수정 일시(재고/메타 갱신 기준) */
    private LocalDateTime updateDate;
}
