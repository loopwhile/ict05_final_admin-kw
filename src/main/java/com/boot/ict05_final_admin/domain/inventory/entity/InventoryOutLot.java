package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 출고-로트 아이템(InventoryOutLot).
 *
 * <p>한 건의 출고 헤더(InventoryOut)와 여러 입고 배치(InventoryBatch, LOT)
 * 간의 할당 결과를 저장하는 조인 엔티티이다. FIFO 계산 결과가 이 단위로 영속된다.</p>
 *
 * <p>정책/무결성:</p>
 * <ul>
 *   <li>동일 출고 헤더 내 동일 배치 중복 금지: {@code uq_outlot_out_batch}</li>
 *   <li>아이템 {@code quantity}의 합계 = 출고 헤더 {@code quantity}</li>
 *   <li>수량 정밀도: DECIMAL(15,3)</li>
 *   <li>잔량 차감은 이 엔티티를 통해서만 수행(서비스 계층 책임)</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "inventory_out_lot",
        indexes = {
                @Index(name = "ix_outlot_out", columnList = "inventory_out_id_fk"),
                @Index(name = "ix_outlot_batch", columnList = "inventory_batch_id_fk")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_outlot_out_batch",
                        columnNames = {"inventory_out_id_fk", "inventory_batch_id_fk"}
                )
        }
)
public class InventoryOutLot {

    /** 출고-로트 행 시퀀스(PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_out_lot_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("출고-로트 행 시퀀스")
    private Long id;

    /** 출고 헤더(FK) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "inventory_out_id_fk",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_outlot_out")
    )
    @Comment("출고 헤더 FK")
    private InventoryOut out;

    /**
     * 배치(LOT) FK.
     *
     * <p>과거 데이터 복구/이관 케이스를 위해 NULL 허용.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "inventory_batch_id_fk",
            foreignKey = @ForeignKey(name = "fk_outlot_batch")
    )
    @Comment("배치(로트) FK")
    private InventoryBatch batch;

    /** 해당 LOT에서 출고된 수량(DECIMAL(15,3)) */
    @Column(name = "quantity", precision = 15, scale = 3, nullable = false)
    @Comment("해당 로트에서 출고된 수량")
    private BigDecimal quantity;

    /** 등록일시(자동 생성) */
    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    @Comment("등록일시")
    private LocalDateTime createdAt;
}
