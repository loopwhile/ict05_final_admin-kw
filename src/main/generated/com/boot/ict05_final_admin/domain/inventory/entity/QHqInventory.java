package com.boot.ict05_final_admin.domain.inventory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHqInventory is a Querydsl query type for HqInventory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHqInventory extends EntityPathBase<HqInventory> {

    private static final long serialVersionUID = -358823635L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHqInventory hqInventory = new QHqInventory("hqInventory");

    public final QInventory _super = new QInventory(this);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMaterial material;

    //inherited
    public final NumberPath<java.math.BigDecimal> optimalQuantity = _super.optimalQuantity;

    //inherited
    public final NumberPath<java.math.BigDecimal> quantity = _super.quantity;

    //inherited
    public final EnumPath<InventoryStatus> status = _super.status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDate = _super.updateDate;

    public QHqInventory(String variable) {
        this(HqInventory.class, forVariable(variable), INITS);
    }

    public QHqInventory(Path<? extends HqInventory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHqInventory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHqInventory(PathMetadata metadata, PathInits inits) {
        this(HqInventory.class, metadata, inits);
    }

    public QHqInventory(Class<? extends HqInventory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.material = inits.isInitialized("material") ? new QMaterial(forProperty("material")) : null;
    }

}

