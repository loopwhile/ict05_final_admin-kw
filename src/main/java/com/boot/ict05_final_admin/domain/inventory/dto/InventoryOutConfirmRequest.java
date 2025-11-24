package com.boot.ict05_final_admin.domain.inventory.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 출고 확정 요청 DTO.
 *
 * <p>출고 확정 API 호출 시 사용한다. 배치 자동할당(FIFO) 또는
 * 배치별 강제 할당(수동)을 모두 지원한다.</p>
 *
 * <p>스케일/정책:</p>
 * <ul>
 *   <li>{@code totalQty} 및 {@code allocation} 수량 값은 DECIMAL(15,3) 스케일 가정</li>
 *   <li>{@code outDate} 미지정 시 서비스에서 now()로 보정 가능</li>
 *   <li>{@code storeId} 미지정 시 내부 사용/폐기 등 비매장 출고로 처리</li>
 *   <li>{@code allocation} 미지정 또는 비어 있으면 FIFO로 자동 배치 할당</li>
 * </ul>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOutConfirmRequest {

    /** 출고 대상 재료 ID */
    private Long materialId;

    /** 출고 대상 가맹점 ID(선택, 내부 사용/폐기 등은 null) */
    private Long storeId;

    /** 총 출고 수량(DECIMAL(15,3)) */
    private BigDecimal totalQty;

    /** 출고 일시(선택, 미전달 시 now 적용) */
    private LocalDateTime outDate;

    /** 비고(선택) */
    private String memo;

    /**
     * 배치별 출고 수량 강제 지정 맵(선택).
     * <p>key: 배치 ID, value: 출고 수량(DECIMAL(15,3))</p>
     * <p>미전달 또는 비어 있으면 서비스에서 FIFO로 자동 배치 할당.</p>
     */
    private Map<Long, BigDecimal> allocation;
}
