package com.boot.ict05_final_admin.domain.receiveOrder.entity;

/**
 * 수주 우선순위 Enum
 *
 * <p>수주의 중요도 및 처리 우선순위를 정의한다.<br>
 * 각 항목은 한글 설명과 함께 표시되며, 화면에서는 {@link #getDescription()} 값을 출력한다.</p>
 *
 * <ul>
 *     <li>{@link #NORMAL} — 일반 우선순위 (기본값)</li>
 *     <li>{@link #URGENT} — 우선 처리 필요 (긴급 주문)</li>
 * </ul>
 *
 * <p>예: “배송 상태 관리”나 “수주 요약 카드”에서 긴급 주문 수량을 표시할 때 사용된다.</p>
 *
 * @author 최민진
 * @since 2025.10
 */
public enum ReceiveOrderPriority {

    /** 일반 우선순위 */
    NORMAL("일반"),

    /** 우선 우선순위 */
    URGENT("우선");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 우선순위의 한글 설명
     */
    ReceiveOrderPriority(String description) { this.description = description; }

    /**
     * 우선순위 한글 설명을 반환한다.
     *
     * @return 우선순위 설명
     */
    public String getDescription() { return description; }
}
