package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

/**
 * 본사 재고(Inventory) 엔티티.
 *
 * <p>
 * 본사 단위의 재고 스냅샷을 보유한다. 재고 수량/적정수량/상태 등 공통 속성은
 * {@link InventoryBase}에서 상속받는다.
 * </p>
 *
 * <p>제약/매핑 규칙:</p>
 * <ul>
 *   <li>{@code uq_inv_material}: 본사 재료별 재고는 1:1로 단일 행만 존재</li>
 *   <li>{@code material}: 본사 재료 엔티티와 1:1(LAZY) 매핑</li>
 *   <li>{@code id}: {@code BIGINT UNSIGNED} auto-increment</li>
 * </ul>
 */
@Entity
@Table(
        name = "inventory",
        uniqueConstraints = @UniqueConstraint(name = "uq_inv_material", columnNames = "material_id_fk")
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SuperBuilder
@Comment("본사 재고")
public class Inventory extends InventoryBase {

    /** 재고 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("재고 ID")
    protected Long id;

    /** 본사 재료(1:1). FK: material.material_id */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "material_id_fk",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_material")
    )
    @Comment("재료")
    protected Material material;
}
