package com.boot.ict05_final_admin.domain.inventory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInventoryIn is a Querydsl query type for InventoryIn
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInventoryIn extends EntityPathBase<InventoryIn> {

    private static final long serialVersionUID = 2117484155L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInventoryIn inventoryIn = new QInventoryIn("inventoryIn");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> inDate = createDateTime("inDate", java.time.LocalDateTime.class);

    public final QMaterial material;

    public final StringPath memo = createString("memo");

    public final NumberPath<java.math.BigDecimal> quantity = createNumber("quantity", java.math.BigDecimal.class);

    public final NumberPath<Long> sellingPrice = createNumber("sellingPrice", Long.class);

    public final NumberPath<java.math.BigDecimal> stockAfter = createNumber("stockAfter", java.math.BigDecimal.class);

    public final com.boot.ict05_final_admin.domain.store.entity.QStore store;

    public final NumberPath<Long> unitPrice = createNumber("unitPrice", Long.class);

    public QInventoryIn(String variable) {
        this(InventoryIn.class, forVariable(variable), INITS);
    }

    public QInventoryIn(Path<? extends InventoryIn> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInventoryIn(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInventoryIn(PathMetadata metadata, PathInits inits) {
        this(InventoryIn.class, metadata, inits);
    }

    public QInventoryIn(Class<? extends InventoryIn> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.material = inits.isInitialized("material") ? new QMaterial(forProperty("material")) : null;
        this.store = inits.isInitialized("store") ? new com.boot.ict05_final_admin.domain.store.entity.QStore(forProperty("store")) : null;
    }

}

