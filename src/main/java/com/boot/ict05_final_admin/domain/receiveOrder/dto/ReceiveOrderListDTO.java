package com.boot.ict05_final_admin.domain.receiveOrder.dto;

import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderPriority;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 수주 목록 조회용 DTO
 *
 * <p>본 DTO는 수주 목록(리스트) 화면 및 API 응답에 사용되며,
 * 각 수주(Receive Order)의 기본 정보를 담는다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *     <li><b>id</b> — 수주 고유 식별자 (PK)</li>
 *     <li><b>storeName</b> — 가맹점명</li>
 *     <li><b>storeLocation</b> — 가맹점 지역명</li>
 *     <li><b>orderCode</b> — 수주 코드번호</li>
 *     <li><b>status</b> — 수주 상태 ({@link ReceiveOrderStatus})</li>
 *     <li><b>priority</b> — 수주 우선순위 ({@link ReceiveOrderPriority})</li>
 *     <li><b>totalPrice</b> — 주문 총액</li>
 *     <li><b>totalCount</b> — 주문 상품 총수량</li>
 *     <li><b>deliveryDate</b> — 배송 예정일</li>
 * </ul>
 *
 * <p>또한 {@link #getStatusDescription()}과 {@link #getPriorityDescription()} 메서드를 통해
 * Enum 값을 한글 설명으로 변환하여 화면에 표시할 수 있다.</p>
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveOrderListDTO {

    /** 수주 시퀀스 */
    private Long id;

    /** 가맹점명 */
    private String storeName;

    /** 가맹점 지역 */
    private String storeLocation;

    /** 수주 코드번호 */
    private String orderCode;

    /** 수주 상태 */
    private ReceiveOrderStatus status;

    /** 수주 우선순위 */
    private ReceiveOrderPriority priority;

    /** 수주 주문 총액 */
    private BigDecimal totalPrice;

    /** 수주 주문 상품 총수량 */
    private Integer totalCount;

    /** 수주 배송 완료일 */
    private LocalDate actualDeliveryDate;

    /**
     * 수주 상태(Enum)의 한글 설명 반환
     *
     * @return 한글 상태 설명 (예: "접수", "배송", "완료", "취소"), 없으면 빈 문자열
     */
    public String getStatusDescription() {
        return status != null ? status.getDescription() : "";
    }

    /**
     * 수주 우선순위(Enum)의 한글 설명 반환
     *
     * @return 한글 우선순위 설명 (예: "일반", "우선"), 없으면 빈 문자열
     */
    public String getPriorityDescription() {
        return priority != null ? priority.getDescription() : "";
    }
}
