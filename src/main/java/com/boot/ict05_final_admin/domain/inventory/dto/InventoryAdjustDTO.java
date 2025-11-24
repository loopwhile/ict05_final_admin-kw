package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.AdjustmentReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고 조정 DTO.
 *
 * <p>
 * - 조정 등록(요청)과 조회(응답)에 공용으로 사용한다.<br>
 * - 상세 조회(getAdjustDetail)의 응답 페이로드도 동일 구조를 따른다.
 * </p>
 *
 * <p>검증/스케일 규칙:</p>
 * <ul>
 *   <li>{@code quantityAfter}는 0 이상(DECIMAL(15,3) 스케일 가정)</li>
 *   <li>{@code difference = quantityAfter - quantityBefore} (서비스 계층에서 계산/검증 가능)</li>
 *   <li>{@code inventoryId}, {@code material}은 필수 식별자</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAdjustDTO {

    // ---------- 팝업/로그용 메타 ----------
    /** 조정 로그 ID(adjustment_id) */
    private Long logId;

    /** 로그 타입 상수값(예: "ADJUST") */
    private String type;

    /** 로그 생성 일시(createdAt) */
    private LocalDateTime logDate;

    // ---------- 조정 본문 ----------
    /** 본사 재고 ID */
    @NotNull
    @Comment("본사 재고 ID")
    private Long inventoryId;

    /**
     * 재료 ID.
     * <p>주의: 필드명은 시스템 호환성으로 {@code material}을 유지한다(= materialId 의미).</p>
     */
    @NotNull
    @Comment("재료 ID")
    private Long material;

    /** 조정 후 수량(0 이상) */
    @NotNull(message = "조정 후 수량을 입력해주세요")
    @PositiveOrZero
    @Comment("조정 후 수량")
    private BigDecimal quantityAfter;

    /** 비고(자유 입력) */
    @Comment("비고 / 조정 사유")
    private String memo;

    /** 조정 사유 분류(MANUAL, DAMAGE, LOSS 등) */
    @Comment("조정 사유 분류")
    private AdjustmentReason reason;

    /** 조정 전 수량 */
    private BigDecimal quantityBefore;

    /** 조정 수량(= after - before) */
    private BigDecimal difference;
}
