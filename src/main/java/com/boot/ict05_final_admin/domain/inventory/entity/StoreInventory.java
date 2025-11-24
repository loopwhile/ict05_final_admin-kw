package com.boot.ict05_final_admin.domain.inventory.entity;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

/**
 * 가맹점 재고(StoreInventory) 엔티티.
 *
 * <p>매장별 현재 재고/적정 수량/상태를 보유한다.
 * 본사 재료가 매장에 매핑된 {@link StoreMaterial} 단위로 1:1 존재하며,
 * 정합성은 (store_id_fk, store_material_id_fk) 유니크로 보장된다.</p>
 */
@Entity
@Table(
        name = "store_inventory",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_store_inv",
                columnNames = {"store_id_fk", "store_material_id_fk"}
        )
)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SuperBuilder
@Getter
@Comment("가맹점 재고")
public class StoreInventory extends InventoryBase {

    /** 가맹점 재고 시퀀스(PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_inventory_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("가맹점 재고 시퀀스")
    private Long id;

    /** 가맹점(FK: store.store_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "store_id_fk",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_si_store")
    )
    @Comment("가맹점 FK")
    private Store store;

    /** 가맹점 재료(FK: store_material.store_material_id) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "store_material_id_fk",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_si_store_material")
    )
    @Comment("가맹점 재료 FK")
    private StoreMaterial storeMaterial;
}
