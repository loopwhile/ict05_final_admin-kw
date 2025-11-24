package com.boot.ict05_final_admin.domain.inventory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreInventory is a Querydsl query type for StoreInventory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreInventory extends EntityPathBase<StoreInventory> {

    private static final long serialVersionUID = -75529119L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreInventory storeInventory = new QStoreInventory("storeInventory");

    public final QInventory _super = new QInventory(this);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<java.math.BigDecimal> optimalQuantity = _super.optimalQuantity;

    //inherited
    public final NumberPath<java.math.BigDecimal> quantity = _super.quantity;

    //inherited
    public final EnumPath<InventoryStatus> status = _super.status;

    public final com.boot.ict05_final_admin.domain.store.entity.QStore store;

    public final QStoreMaterial storeMaterial;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDate = _super.updateDate;

    public QStoreInventory(String variable) {
        this(StoreInventory.class, forVariable(variable), INITS);
    }

    public QStoreInventory(Path<? extends StoreInventory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreInventory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreInventory(PathMetadata metadata, PathInits inits) {
        this(StoreInventory.class, metadata, inits);
    }

    public QStoreInventory(Class<? extends StoreInventory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new com.boot.ict05_final_admin.domain.store.entity.QStore(forProperty("store")) : null;
        this.storeMaterial = inits.isInitialized("storeMaterial") ? new QStoreMaterial(forProperty("storeMaterial"), inits.get("storeMaterial")) : null;
    }

}

