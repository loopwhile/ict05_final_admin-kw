package com.boot.ict05_final_admin.domain.inventory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreMaterial is a Querydsl query type for StoreMaterial
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreMaterial extends EntityPathBase<StoreMaterial> {

    private static final long serialVersionUID = -1300757118L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreMaterial storeMaterial = new QStoreMaterial("storeMaterial");

    public final StringPath baseUnit = createString("baseUnit");

    public final StringPath category = createString("category");

    public final StringPath code = createString("code");

    public final DatePath<java.time.LocalDate> expirationDate = createDate("expirationDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isHqMaterial = createBoolean("isHqMaterial");

    public final QMaterial material;

    public final DateTimePath<java.time.LocalDateTime> modifyDate = createDateTime("modifyDate", java.time.LocalDateTime.class);

    public final StringPath name = createString("name");

    public final NumberPath<java.math.BigDecimal> optimalQuantity = createNumber("optimalQuantity", java.math.BigDecimal.class);

    public final NumberPath<Long> purchasePrice = createNumber("purchasePrice", Long.class);

    public final NumberPath<java.math.BigDecimal> quantity = createNumber("quantity", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> regDate = createDateTime("regDate", java.time.LocalDateTime.class);

    public final StringPath salesUnit = createString("salesUnit");

    public final NumberPath<Long> sellingPrice = createNumber("sellingPrice", Long.class);

    public final EnumPath<MaterialStatus> status = createEnum("status", MaterialStatus.class);

    public final com.boot.ict05_final_admin.domain.store.entity.QStore store;

    public final StringPath supplier = createString("supplier");

    public final EnumPath<MaterialTemperature> temperature = createEnum("temperature", MaterialTemperature.class);

    public QStoreMaterial(String variable) {
        this(StoreMaterial.class, forVariable(variable), INITS);
    }

    public QStoreMaterial(Path<? extends StoreMaterial> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreMaterial(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreMaterial(PathMetadata metadata, PathInits inits) {
        this(StoreMaterial.class, metadata, inits);
    }

    public QStoreMaterial(Class<? extends StoreMaterial> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.material = inits.isInitialized("material") ? new QMaterial(forProperty("material")) : null;
        this.store = inits.isInitialized("store") ? new com.boot.ict05_final_admin.domain.store.entity.QStore(forProperty("store")) : null;
    }

}

