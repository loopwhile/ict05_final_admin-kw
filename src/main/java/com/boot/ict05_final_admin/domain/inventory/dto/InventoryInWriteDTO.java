package com.boot.ict05_final_admin.domain.inventory.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 입고 등록 DTO (본사/가맹점 공용).
 *
 * <p>
 * - 본사 입고: {@code storeId} 생략 가능(본사 재고 기준).<br>
 * - 가맹점 직입고: {@code storeId} 지정.
 * </p>
 *
 * <p>스케일/형식 규칙:</p>
 * <ul>
 *   <li>{@code quantity}, {@code unitPrice}, {@code sellingPrice}: DECIMAL(15,3) 스케일 가정</li>
 *   <li>{@code inDate}: ISO-8601 LocalDateTime (예: {@code 2025-11-21T13:00:00})</li>
 *   <li>{@code expirationDate}: ISO-8601 LocalDate (예: {@code 2025-12-31})</li>
 * </ul>
 *
 * <p>단가 정책:</p>
 * <ul>
 *   <li>{@code unitPrice}: 본사 매입가(구매단가), 배치(LOT) 생성 시 단가이력 연동 정책은 서비스 계층에서 처리</li>
 *   <li>{@code sellingPrice}: 가맹점 공급가(선택), 존재 시 별도의 단가이력 정책 적용은 서비스 계층에서 처리</li>
 * </ul>
 */
@Data
public class InventoryInWriteDTO {

    /** 입고 대상 재료 ID */
    private Long materialId;

    /** 입고 수량(DECIMAL(15,3)) */
    private BigDecimal quantity;

    /** 입고 단가(본사 매입가, DECIMAL(15,3)) */
    private BigDecimal unitPrice;

    /** 가맹점 공급가(선택, DECIMAL(15,3)) */
    private BigDecimal sellingPrice;

    /** 입고일(ISO-8601 LocalDateTime) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime inDate;

    /** 유통기한(ISO-8601 LocalDate) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expirationDate;

    /** 비고(선택) */
    private String memo;

    /** 가맹점 ID(선택, 가맹점 직입고 시 지정) */
    private Long storeId;
}
