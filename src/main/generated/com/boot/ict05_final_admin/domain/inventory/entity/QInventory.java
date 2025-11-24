package com.boot.ict05_final_admin.domain.inventory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInventory is a Querydsl query type for Inventory
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QInventory extends EntityPathBase<Inventory> {

    private static final long serialVersionUID = -1450308938L;

    public static final QInventory inventory = new QInventory("inventory");

    public final NumberPath<java.math.BigDecimal> optimalQuantity = createNumber("optimalQuantity", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> quantity = createNumber("quantity", java.math.BigDecimal.class);

    public final EnumPath<InventoryStatus> status = createEnum("status", InventoryStatus.class);

    public final DateTimePath<java.time.LocalDateTime> updateDate = createDateTime("updateDate", java.time.LocalDateTime.class);

    public QInventory(String variable) {
        super(Inventory.class, forVariable(variable));
    }

    public QInventory(Path<? extends Inventory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInventory(PathMetadata metadata) {
        super(Inventory.class, metadata);
    }

}

