package com.boot.ict05_final_admin.domain.store.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStore is a Querydsl query type for Store
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStore extends EntityPathBase<Store> {

    private static final long serialVersionUID = -1065395882L;

    public static final QStore store = new QStore("store");

    public final NumberPath<java.math.BigDecimal> affiliatePrice = createNumber("affiliatePrice", java.math.BigDecimal.class);

    public final StringPath businessRegistrationNumber = createString("businessRegistrationNumber");

    public final StringPath comment = createString("comment");

    public final DatePath<java.time.LocalDate> contractAffiliateDate = createDate("contractAffiliateDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> contractStartDate = createDate("contractStartDate", java.time.LocalDate.class);

    public final NumberPath<Integer> contractTerm = createNumber("contractTerm", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath location = createString("location");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final NumberPath<java.math.BigDecimal> monthlySales = createNumber("monthlySales", java.math.BigDecimal.class);

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    public final NumberPath<java.math.BigDecimal> royalty = createNumber("royalty", java.math.BigDecimal.class);

    public final NumberPath<Long> staffId = createNumber("staffId", Long.class);

    public final EnumPath<StoreStatus> status = createEnum("status", StoreStatus.class);

    public final EnumPath<StoreType> type = createEnum("type", StoreType.class);

    public QStore(String variable) {
        super(Store.class, forVariable(variable));
    }

    public QStore(Path<? extends Store> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStore(PathMetadata metadata) {
        super(Store.class, metadata);
    }

}

