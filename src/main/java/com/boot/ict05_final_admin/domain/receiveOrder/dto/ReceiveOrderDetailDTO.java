package com.boot.ict05_final_admin.domain.receiveOrder.dto;

import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderPriority;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 수주 상세 조회용 DTO
 *
 * <p>수주(Receive Order)의 주요 정보를 포함하며,
 * 수주 단건 상세 화면 또는 API 응답에 사용된다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *     <li><b>id</b> — 수주의 고유 식별자 (PK)</li>
 *     <li><b>orderCode</b> — 수주 코드 번호</li>
 *     <li><b>orderDate</b> — 발주일</li>
 *     <li><b>deliveryDate</b> — 배송 예정일</li>
 *     <li><b>status</b> — 수주 진행 상태 ({@link ReceiveOrderStatus})</li>
 *     <li><b>priority</b> — 수주 우선순위 ({@link ReceiveOrderPriority})</li>
 *     <li><b>storeName</b> — 가맹점명</li>
 *     <li><b>storeId</b> — 가맹점 고유 ID</li>
 *     <li><b>storeLocation</b> — 가맹점 지역명</li>
 *     <li><b>totalPrice</b> — 수주 총액</li>
 *     <li><b>totalCount</b> — 수주 총수량</li>
 *     <li><b>remark</b> — 수주 비고 또는 특이사항</li>
 *     <li><b>items</b> — 하위 주문 상품 목록 ({@link ReceiveOrderItemDTO})</li>
 * </ul>
 *
 * <p>또한 {@link #getStatusDescription()}과 {@link #getPriorityDescription()}을 통해
 * Enum 값을 한글 설명으로 변환하여 화면에 표시할 수 있다.</p>
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveOrderDetailDTO {

    /** 수주 상세 시퀀스 */
    private Long id;

    /** 수주 코드번호 */
    private String orderCode;

    /** 발주일 */
    private LocalDate orderDate;

    /** 수주 배송 완료일 */
    private LocalDate actualDeliveryDate;

    /** 수주 상태 */
    private ReceiveOrderStatus status;

    /** 수주 우선순위 */
    private ReceiveOrderPriority priority;

    /** 가맹점명 */
    private String storeName;   // 가맹점 엔티티

    /** 가맹점 코드 */
    private Long storeId;       // 가맹점 엔티티

    /** 가맹점 지역 */
    private String storeLocation;   // 가맹점 엔티티

    /** 발주 총액 */
    private BigDecimal totalPrice;

    /** 발주 총수량 */
    private Integer totalCount;

    /** 수주 특이사항(비고) */
    private String remark;

    /** 하위 주문 상품 목록 */
    private List<ReceiveOrderItemDTO> items;

    public void setItems(List<ReceiveOrderItemDTO> items) {
        this.items = items;
    }

    /* 화면에 한글 값으로 출력 */
    public String getStatusDescription() {
        return status != null ? status.getDescription() : "";
    }

    public String getPriorityDescription() {
        return priority != null ? priority.getDescription() : "";
    }

}
