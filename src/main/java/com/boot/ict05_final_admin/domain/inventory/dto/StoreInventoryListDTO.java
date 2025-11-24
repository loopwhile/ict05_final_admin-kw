package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 가맹점 재고 목록 DTO.
 *
 * <p>본사에서 가맹점별 재고 현황을 조회할 때 목록(grid) 행을 구성하는 요약 정보 컨테이너.</p>
 *
 * <p>필드 규칙:</p>
 * <ul>
 *   <li>{@code quantity}, {@code optimalQuantity}: DECIMAL(15,3) 스케일 가정</li>
 *   <li>{@code status}: {@link InventoryStatus} (SUFFICIENT/LOW/SHORTAGE 등)</li>
 *   <li>{@code updateDate}: 재고/메타 갱신 최신 시각(서버/DB 타임존 정책 따름)</li>
 * </ul>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreInventoryListDTO {

    /** 재고 ID */
    private Long id;

    /** 가맹점 ID */
    private Long storeId;

    /** 가맹점명 */
    private String storeName;

    /** 재료명 */
    private String materialName;

    /** 카테고리명 */
    private String categoryName;

    /** 현재 수량(DECIMAL(15,3)) */
    private BigDecimal quantity;

    /** 적정 수량(DECIMAL(15,3)) */
    private BigDecimal optimalQuantity;

    /** 재고 상태(SUFFICIENT/LOW/SHORTAGE) */
    private InventoryStatus status;

    /** 마지막 수정 일시 */
    private LocalDateTime updateDate;
}
