package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 본사 재고 수량 조정 이력.
 *
 * <p>입출고 외 사유(분실, 파손, 오입력 등)로 재고를 직접 수정한 내역을 저장한다.
 * v_inventory_log 뷰와 UNION 되어 입출고 내역과 함께 조회된다.</p>
 */
@Entity
@Table(name = "inventory_adjustment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustment {

    /** 재고 조정 시퀀스(PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adjustment_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("재고 조정 시퀀스")
    private Long id;

    /** 본사 재고 FK */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "inventory_id_fk",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_adjustment_inventory")
    )
    @Comment("본사 재고 FK")
    private Inventory inventory;

    /** 조정 전 수량 */
    @Column(name = "inventory_adjustment_quantity_before", precision = 15, scale = 3, nullable = false)
    @Comment("조정 전 수량")
    private BigDecimal quantityBefore;

    /** 조정 후 수량 */
    @Column(name = "inventory_adjustment_quantity_after", precision = 15, scale = 3, nullable = false)
    @Comment("조정 후 수량")
    private BigDecimal quantityAfter;

    /** 증감 수량(후 - 전) */
    @Column(name = "inventory_adjustment_difference", precision = 15, scale = 3, nullable = false)
    @Comment("증감 수량")
    private BigDecimal difference;

    /** 조정 단가(선택) */
    @Column(name = "inventory_adjustment_unit_price", precision = 15, scale = 3)
    @Comment("조정 단가")
    private BigDecimal unitPrice;

    /** 비고 / 조정 사유(자유 입력) */
    @Column(name = "inventory_adjustment_memo", length = 255)
    @Comment("비고 / 조정 사유")
    private String memo;

    /** 조정 일시 */
    @CreationTimestamp
    @Column(name = "inventory_adjustment_created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    @Comment("조정일시")
    private LocalDateTime createdAt;

    /** 조정 사유 분류(MANUAL, DAMAGE, LOSS, ERROR) */
    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_adjustment_reason", length = 20)
    @Comment("조정 사유 (MANUAL, DAMAGE, LOSS, ERROR)")
    private AdjustmentReason reason;

    /** 트랜잭션 상태(DRAFT/CONFIRMED/CANCELLED/REVERSED) */
    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_record_status", nullable = false, length = 20)
    @Builder.Default
    @Comment("트랜잭션 상태(DRAFT/CONFIRMED/CANCELLED/REVERSED)")
    private InventoryRecordStatus status = InventoryRecordStatus.CONFIRMED;
}
