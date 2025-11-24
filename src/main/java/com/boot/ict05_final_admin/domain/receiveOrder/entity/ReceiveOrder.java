package com.boot.ict05_final_admin.domain.receiveOrder.entity;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 본사 수주 엔티티 (Receive Order)
 *
 * <p>가맹점의 발주 내역을 기반으로 본사에서 생성되는 수주(Purchase Order) 정보를 저장한다.</p>
 *
 * <p>본 엔티티는 다음과 같은 정보를 포함한다:</p>
 * <ul>
 *     <li>가맹점 정보 ({@link Store})</li>
 *     <li>수주 코드, 수주일, 배송 예정일, 실제 납기일</li>
 *     <li>총 주문 금액, 총 수량, 상태, 우선순위</li>
 *     <li>하위 상세 항목 리스트 ({@link ReceiveOrderDetail})</li>
 * </ul>
 *
 * <p>DB 테이블명: <b>receive_order</b></p>
 *
 * @author 최민진
 * @since 2025.10
 */
@Entity
@Table(name = "purchase_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiveOrder {

    /** 수주 시퀀스 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_id", columnDefinition = "INT UNSIGNED")
    private Long id;

    /** 가맹점 시퀀스 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id_fk", nullable = false)
    private Store store;

    /** 수주 번호 */
    @Column(name = "purchase_order_code", length = 32, nullable = false, unique = true)
    private String orderCode;

    /** 수주일 */
    @Column(name = "purchase_order_date", nullable = false)
    private LocalDate orderDate;

    /** 수주 총액 */
    @Column(name = "purchase_order_total_price", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO;

    /** 수주 총개수 */
    @Column(name = "purchase_order_total_count", columnDefinition = "INT UNSIGNED DEFAULT 0 COMMENT '발주 총개수'")
    private Integer totalCount;

    /** 수주 비고 */
    @Column(name = "purchase_order_remark", columnDefinition = "TEXT")
    private String remark;

    /** 수주 공급업체명 */
    @Column(name = "purchase_order_supplier", length = 100, nullable = false)
    private String supplier;

    /** 수주 상태 */
    @Column(name = "purchase_order_status")
    @Enumerated(EnumType.STRING)
    private ReceiveOrderStatus status;

    /** 수주 우선순위 */
    @Column(name = "purchase_order_priority")
    @Enumerated(EnumType.STRING)
    private ReceiveOrderPriority priority;

    /** 수주 배송 예정일 */
    @Column(name = "purchase_order_delivery_date")
    private LocalDate deliveryDate;

    /** 수주 실제 납기일 */
    @Column(name = "purchase_order_actual_delivery_date")
    private LocalDate actualDeliveryDate;

    /** 수주 상세 목록 (1:N 관계) */
    @OneToMany(mappedBy = "receiveOrder")
    private List<ReceiveOrderDetail> details;

}
