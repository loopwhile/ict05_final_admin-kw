package com.boot.ict05_final_admin.domain.receiveOrder.entity;

import com.boot.ict05_final_admin.domain.inventory.entity.Inventory;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.StoreMaterial;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 본사 수주 상세 엔티티 (Receive Order Detail)
 *
 * <p>가맹점 발주 상세 내역을 기반으로 본사에서 생성되는
 * 개별 수주 품목 정보를 관리한다.</p>
 *
 * <p>각 {@link ReceiveOrder}와 연결되어 있으며,
 * 실제 수주에 포함된 재료({@link Material})와 본사 재고({@link Inventory})를 참조한다.</p>
 *
 * <p>DB 테이블명: <b>receive_order_detail</b></p>
 *
 * @author 최민진
 * @since 2025.10
 */
@Entity
@Table(name = "purchase_order_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiveOrderDetail {

    /** 수주 상세 시퀀스 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_detail_id")
    private Long id;

    /** 수주 시퀀스 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id_fk", nullable = false)
    private ReceiveOrder receiveOrder;

    /** 재료 재고 시퀀스 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id_fk", nullable = false)
    private Inventory inventory;

    /** 재료 시퀀스 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id_fk", nullable = false)
    private StoreMaterial storeMaterial;

    /** 수주 단가 */
    @Column(name = "purchase_order_detail_unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    /** 수주 수량 */
    @Column(name = "purchase_order_detail_count", nullable = false)
    private Integer count;

    /** 수주 단가 총액 */
    @Column(name = "purchase_order_detail_total_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalPrice;

}
