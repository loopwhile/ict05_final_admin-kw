package com.boot.ict05_final_admin.domain.order.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCustomerOrder is a Querydsl query type for CustomerOrder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCustomerOrder extends EntityPathBase<CustomerOrder> {

    private static final long serialVersionUID = -1633544904L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCustomerOrder customerOrder = new QCustomerOrder("customerOrder");

    public final NumberPath<java.math.BigDecimal> discount = createNumber("discount", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath memo = createString("memo");

    public final StringPath orderCode = createString("orderCode");

    public final DateTimePath<java.time.LocalDateTime> orderedAt = createDateTime("orderedAt", java.time.LocalDateTime.class);

    public final EnumPath<OrderType> orderType = createEnum("orderType", OrderType.class);

    public final EnumPath<PaymentType> paymentType = createEnum("paymentType", PaymentType.class);

    public final EnumPath<OrderStatus> status = createEnum("status", OrderStatus.class);

    public final com.boot.ict05_final_admin.domain.store.entity.QStore storeIdFk;

    public final NumberPath<java.math.BigDecimal> totalPrice = createNumber("totalPrice", java.math.BigDecimal.class);

    public QCustomerOrder(String variable) {
        this(CustomerOrder.class, forVariable(variable), INITS);
    }

    public QCustomerOrder(Path<? extends CustomerOrder> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCustomerOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCustomerOrder(PathMetadata metadata, PathInits inits) {
        this(CustomerOrder.class, metadata, inits);
    }

    public QCustomerOrder(Class<? extends CustomerOrder> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.storeIdFk = inits.isInitialized("storeIdFk") ? new com.boot.ict05_final_admin.domain.store.entity.QStore(forProperty("storeIdFk")) : null;
    }

}

