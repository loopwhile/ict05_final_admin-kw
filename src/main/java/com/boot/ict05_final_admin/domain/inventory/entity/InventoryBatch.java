package com.boot.ict05_final_admin.domain.inventory.entity;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 재고 배치(입고분, LOT) 엔티티.
 *
 * <p>{@code inventory_batch}와 매핑되며, 재료의 입고 단위(LOT)별 수량/단가/유통기한을 관리한다.
 * 각 입고 시 1건 생성되고, 출고/조정 경로를 통해 {@code quantity}가 감소한다.</p>
 *
 * <p>정밀도 규칙:</p>
 * <ul>
 *   <li>{@code receivedQuantity}, {@code quantity}: DECIMAL(15,3)</li>
 *   <li>{@code unitPrice}: DECIMAL(15,2)</li>
 * </ul>
 *
 * <p>변경 정책: 잔량({@code quantity})는 서비스 계층(출고/조정)에서만 변경한다.</p>
 *
 * @author 김주연
 * @since 2025-11-05
 */
@Entity
@Table(name = "inventory_batch")
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBatch {

    /** 재고 배치 시퀀스(PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_batch_id", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    @Comment("재고 배치 시퀀스")
    private Long id;

    /** LOT 번호(전역 유니크) */
    @Column(name = "inventory_batch_lot_no", length = 32, nullable = false, unique = true)
    @Comment("로트 번호")
    private String lotNo;

    /** 소유 주체(가맹점 또는 본사). null=본사 LOT */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "store_id_fk",
            columnDefinition = "BIGINT UNSIGNED",
            foreignKey = @ForeignKey(name = "fk_si_store")
    )
    @Comment("가맹점(FK) 또는 본사")
    private Store store;

    /** 재료 FK */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id_fk", referencedColumnName = "material_id", nullable = false)
    @Comment("재료 시퀀스(FK)")
    private Material material;

    /** 입고 일시(실제 입고 시간) */
    @Column(name = "inventory_batch_received_date", nullable = false, columnDefinition = "DATETIME")
    @Comment("입고일시")
    @Builder.Default
    private LocalDateTime receivedDate = LocalDateTime.now();

    /** 입고 단가(본사 매입가, DECIMAL(15,2)) */
    @Column(name = "inventory_batch_unit_price", precision = 15, scale = 2, nullable = false)
    @Comment("해당 로트의 입고 단가(본사 매입가)")
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /** 유통기한(일 단위) */
    @Column(name = "inventory_batch_expiration_date", nullable = false, columnDefinition = "DATE")
    @Comment("유통기한")
    private LocalDate expirationDate;

    /** 입고 당시 수량(DECIMAL(15,3)) */
    @Column(
            name = "inventory_batch_received_quantity",
            precision = 15, scale = 3, nullable = false,
            columnDefinition = "DECIMAL(15,3) DEFAULT 0"
    )
    @Comment("입고 당시 수량")
    @Builder.Default
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    /** 현재 남은 수량(DECIMAL(15,3)) */
    @Column(
            name = "inventory_batch_quantity",
            precision = 15, scale = 3, nullable = false,
            columnDefinition = "DECIMAL(15,3) DEFAULT 0"
    )
    @Comment("현재 남은 수량")
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ZERO;

    /** 등록일시(자동 생성) */
    @CreationTimestamp
    @Column(name = "inventory_batch_created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    @Comment("등록일시 (자동 생성)")
    private LocalDateTime createdAt;

    /**
     * 잔량 차감(가드 포함).
     *
     * <p>0 이하 또는 null 입력은 무시한다. 차감 결과가 음수면 {@link IllegalArgumentException}을 던진다.</p>
     *
     * @param amount 차감할 수량(DECIMAL(15,3), 양수)
     */
    public void subtractQuantity(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) return;
        if (this.quantity == null) this.quantity = BigDecimal.ZERO;

        BigDecimal after = this.quantity.subtract(amount);
        if (after.signum() < 0) {
            throw new IllegalArgumentException(
                    "배치 잔량 부족: batchId=" + id + ", 현재=" + this.quantity + ", 요청=" + amount
            );
        }
        this.quantity = after;
    }
}
