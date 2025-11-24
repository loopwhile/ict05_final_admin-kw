package com.boot.ict05_final_admin.domain.receiveOrder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 수주 요약 정보 DTO
 *
 * <p>수주 현황 상단 카드에서 전체 통계 정보를 표시하기 위한 DTO이다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *     <li><b>totalCount</b> — 전체 주문 개수</li>
 *     <li><b>totalAmount</b> — 전체 주문 총액</li>
 *     <li><b>shippingCount</b> — 현재 배송 중인 주문 개수</li>
 *     <li><b>urgentCount</b> — 우선순위가 “긴급(URGENT)”인 주문 개수</li>
 * </ul>
 *
 * <p>이 DTO는 주로 {@code ReceiveOrderService.getSummary()} 결과를 통해
 * 수주현황 페이지 상단 통계 카드에 표시된다.</p>
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveOrderSummaryDTO {

    /** 전체 주문 개수 */
    private Long totalCount;

    /** 총 주문액 */
    private BigDecimal totalAmount;

    /** 배송 중인 상품 개수 */
    private Long shippingCount;

    /** 긴급 주문 개수 */
    private Long urgentCount;
}

