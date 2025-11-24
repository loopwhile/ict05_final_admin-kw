package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FIFO 후보 배치 DTO.
 *
 * <p>출고 미리보기(FIFO) 계산을 위해 재고 잔량이 남아있는 배치(LOT)를 표현한다.
 * 서비스 계층에서 유통기한/입고일 기준 정렬 우선순위를 적용해 사용한다.</p>
 *
 * <p>필드 규칙:</p>
 * <ul>
 *   <li>{@code batchId}: 배치 ID</li>
 *   <li>{@code lotNo}: LOT 번호</li>
 *   <li>{@code expirationDate}: 유통기한(선택, 없을 수 있음)</li>
 *   <li>{@code available}: 현재 배치 잔량(DECIMAL(15,3) 스케일 준수)</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FifoCandidateDTO {

    /** 배치 ID */
    private Long batchId;

    /** LOT 번호 */
    private String lotNo;

    /** 유통기한(선택) */
    @Schema(type = "string", format = "date")
    private LocalDate expirationDate;

    /** 현재 배치 잔량(DECIMAL(15,3)) */
    private BigDecimal available;
}
