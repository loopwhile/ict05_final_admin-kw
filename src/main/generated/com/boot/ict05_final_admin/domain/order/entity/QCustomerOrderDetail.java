package com.boot.ict05_final_admin.domain.order.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCustomerOrderDetail is a Querydsl query type for CustomerOrderDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCustomerOrderDetail extends EntityPathBase<CustomerOrderDetail> {

    private static final long serialVersionUID = -1943384151L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCustomerOrderDetail customerOrderDetail = new QCustomerOrderDetail("customerOrderDetail");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigDecimal> lineTotal = createNumber("lineTotal", java.math.BigDecimal.class);

    public final NumberPath<Long> menuIdFk = createNumber("menuIdFk", Long.class);

    public final QCustomerOrder order;

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<java.math.BigDecimal> unitPrice = createNumber("unitPrice", java.math.BigDecimal.class);

    public QCustomerOrderDetail(String variable) {
        this(CustomerOrderDetail.class, forVariable(variable), INITS);
    }

    public QCustomerOrderDetail(Path<? extends CustomerOrderDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCustomerOrderDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCustomerOrderDetail(PathMetadata metadata, PathInits inits) {
        this(CustomerOrderDetail.class, metadata, inits);
    }

    public QCustomerOrderDetail(Class<? extends CustomerOrderDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new QCustomerOrder(forProperty("order"), inits.get("order")) : null;
    }

}

