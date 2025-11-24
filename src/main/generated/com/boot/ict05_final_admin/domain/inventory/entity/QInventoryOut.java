package com.boot.ict05_final_admin.domain.inventory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInventoryOut is a Querydsl query type for InventoryOut
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInventoryOut extends EntityPathBase<InventoryOut> {

    private static final long serialVersionUID = 1217505464L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInventoryOut inventoryOut = new QInventoryOut("inventoryOut");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMaterial material;

    public final StringPath memo = createString("memo");

    public final DateTimePath<java.time.LocalDateTime> outDate = createDateTime("outDate", java.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> quantity = createNumber("quantity", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> stockAfter = createNumber("stockAfter", java.math.BigDecimal.class);

    public final com.boot.ict05_final_admin.domain.store.entity.QStore store;

    public final NumberPath<Long> unitPrice = createNumber("unitPrice", Long.class);

    public QInventoryOut(String variable) {
        this(InventoryOut.class, forVariable(variable), INITS);
    }

    public QInventoryOut(Path<? extends InventoryOut> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInventoryOut(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInventoryOut(PathMetadata metadata, PathInits inits) {
        this(InventoryOut.class, metadata, inits);
    }

    public QInventoryOut(Class<? extends InventoryOut> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.material = inits.isInitialized("material") ? new QMaterial(forProperty("material")) : null;
        this.store = inits.isInitialized("store") ? new com.boot.ict05_final_admin.domain.store.entity.QStore(forProperty("store")) : null;
    }

}

