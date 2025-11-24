package com.boot.ict05_final_admin.domain.receiveOrder.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReceiveOrderDetail is a Querydsl query type for ReceiveOrderDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReceiveOrderDetail extends EntityPathBase<ReceiveOrderDetail> {

    private static final long serialVersionUID = 474243927L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReceiveOrderDetail receiveOrderDetail = new QReceiveOrderDetail("receiveOrderDetail");

    public final NumberPath<Integer> detailCount = createNumber("detailCount", Integer.class);

    public final NumberPath<Long> detailId = createNumber("detailId", Long.class);

    public final NumberPath<java.math.BigDecimal> detailTotalPrice = createNumber("detailTotalPrice", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> detailUnitPrice = createNumber("detailUnitPrice", java.math.BigDecimal.class);

    public final com.boot.ict05_final_admin.domain.inventory.entity.QHqInventory hqInventory;

    public final com.boot.ict05_final_admin.domain.inventory.entity.QMaterial material;

    public final QReceiveOrder receiveOrder;

    public QReceiveOrderDetail(String variable) {
        this(ReceiveOrderDetail.class, forVariable(variable), INITS);
    }

    public QReceiveOrderDetail(Path<? extends ReceiveOrderDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReceiveOrderDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReceiveOrderDetail(PathMetadata metadata, PathInits inits) {
        this(ReceiveOrderDetail.class, metadata, inits);
    }

    public QReceiveOrderDetail(Class<? extends ReceiveOrderDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.hqInventory = inits.isInitialized("hqInventory") ? new com.boot.ict05_final_admin.domain.inventory.entity.QHqInventory(forProperty("hqInventory"), inits.get("hqInventory")) : null;
        this.material = inits.isInitialized("material") ? new com.boot.ict05_final_admin.domain.inventory.entity.QMaterial(forProperty("material")) : null;
        this.receiveOrder = inits.isInitialized("receiveOrder") ? new QReceiveOrder(forProperty("receiveOrder"), inits.get("receiveOrder")) : null;
    }

}

