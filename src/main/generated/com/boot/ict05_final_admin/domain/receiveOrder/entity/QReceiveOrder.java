package com.boot.ict05_final_admin.domain.receiveOrder.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReceiveOrder is a Querydsl query type for ReceiveOrder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReceiveOrder extends EntityPathBase<ReceiveOrder> {

    private static final long serialVersionUID = -2113404058L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReceiveOrder receiveOrder = new QReceiveOrder("receiveOrder");

    public final DatePath<java.time.LocalDate> actualDeliveryDate = createDate("actualDeliveryDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> deliveryDate = createDate("deliveryDate", java.time.LocalDate.class);

    public final ListPath<ReceiveOrderDetail, QReceiveOrderDetail> details = this.<ReceiveOrderDetail, QReceiveOrderDetail>createList("details", ReceiveOrderDetail.class, QReceiveOrderDetail.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath orderCode = createString("orderCode");

    public final DatePath<java.time.LocalDate> orderDate = createDate("orderDate", java.time.LocalDate.class);

    public final EnumPath<ReceiveOrderPriority> priority = createEnum("priority", ReceiveOrderPriority.class);

    public final StringPath remark = createString("remark");

    public final EnumPath<ReceiveOrderStatus> status = createEnum("status", ReceiveOrderStatus.class);

    public final com.boot.ict05_final_admin.domain.store.entity.QStore store;

    public final StringPath supplier = createString("supplier");

    public final NumberPath<Integer> totalCount = createNumber("totalCount", Integer.class);

    public final NumberPath<java.math.BigDecimal> totalPrice = createNumber("totalPrice", java.math.BigDecimal.class);

    public QReceiveOrder(String variable) {
        this(ReceiveOrder.class, forVariable(variable), INITS);
    }

    public QReceiveOrder(Path<? extends ReceiveOrder> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReceiveOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReceiveOrder(PathMetadata metadata, PathInits inits) {
        this(ReceiveOrder.class, metadata, inits);
    }

    public QReceiveOrder(Class<? extends ReceiveOrder> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new com.boot.ict05_final_admin.domain.store.entity.QStore(forProperty("store")) : null;
    }

}

