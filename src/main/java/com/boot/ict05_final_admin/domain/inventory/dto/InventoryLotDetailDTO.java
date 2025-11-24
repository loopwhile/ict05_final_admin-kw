package com.boot.ict05_final_admin.domain.inventory.dto;

import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 본사 재고 LOT 상세 DTO.
 *
 * <p>{@code inventory_batch} 단일 행(배치/LOT)에 대한 상세 정보를 담는다.</p>
 *
 * <p>스케일/형식 규칙:</p>
 * <ul>
 *   <li>{@code receivedQuantity}, {@code remainingQuantity}, {@code unitPrice}: DECIMAL(15,3) 스케일 가정</li>
 *   <li>{@code receivedDate}: ISO-8601 LocalDateTime</li>
 *   <li>{@code expirationDate}: ISO-8601 LocalDate</li>
 * </ul>
 */
@Data
public class InventoryLotDetailDTO {

    /** 배치 ID */
    @Comment("배치 ID")
    private Long batchId;

    /** 재료 ID */
    @Comment("재료 ID")
    private Long materialId;

    /** 재료 코드 */
    @Comment("재료 코드")
    private String materialCode;

    /** 재료명 */
    @Comment("재료명")
    private String materialName;

    /** LOT 번호 */
    @Comment("LOT 번호")
    private String lotNo;

    /** 입고 일시(ISO-8601 LocalDateTime) */
    @Comment("입고일시")
    private LocalDateTime receivedDate;

    /** 유통기한(ISO-8601 LocalDate) */
    @Comment("유통기한")
    private LocalDate expirationDate;

    /** 입고 수량(DECIMAL(15,3)) */
    @Comment("입고 수량")
    private BigDecimal receivedQuantity;

    /** 현재 잔량(DECIMAL(15,3)) */
    @Comment("현재 잔량")
    private BigDecimal remainingQuantity;

    /** 입고 단가/매입가(DECIMAL(15,3)) */
    @Comment("단가")
    private BigDecimal unitPrice;

    // ---------- 로그 팝업 상단 표시용(선택) ----------
    /** 연동된 로그 ID (v_inventory_log 에서 전달되는 경우 세팅) */
    private Long logId;

    /** 로그 유형(예: "INCOME", "OUTGO") */
    private String type;

    /** 해당 로그 발생 시각 */
    private LocalDateTime logDate;
}
