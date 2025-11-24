package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 출고 FIFO 분배 미리보기/확정용 DTO.
 *
 * <p>요청 수량을 각 배치(LOT)에 분배한 결과를 표현한다.
 * 서비스 계층에서 FIFO 규칙(입고일/유통기한 우선순위)을 적용해 생성한다.</p>
 *
 * <p>필드 규칙:</p>
 * <ul>
 *   <li>{@code batchId}: 배치 고유 ID</li>
 *   <li>{@code lotNo}: LOT 번호</li>
 *   <li>{@code expirationDate}: 유통기한(ISO-8601 LocalDate, 선택)</li>
 *   <li>{@code qty}: 해당 배치에서 출고할 수량(DECIMAL(15,3) 스케일 가정)</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InventoryOutPreviewItemDTO {

    /** 배치 고유 ID */
    private Long batchId;

    /** LOT 번호 */
    private String lotNo;

    /** 유통기한(ISO-8601 LocalDate) */
    @Schema(type = "string", format = "date")
    private LocalDate expirationDate;

    /** 해당 배치에서 출고할 수량(DECIMAL(15,3)) */
    private BigDecimal qty;
}
