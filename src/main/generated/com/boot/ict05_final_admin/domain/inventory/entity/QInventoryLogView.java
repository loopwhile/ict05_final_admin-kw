package com.boot.ict05_final_admin.domain.inventory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInventoryLogView is a Querydsl query type for InventoryLogView
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInventoryLogView extends EntityPathBase<InventoryLogView> {

    private static final long serialVersionUID = 941638771L;

    public static final QInventoryLogView inventoryLogView = new QInventoryLogView("inventoryLogView");

    public final DateTimePath<java.time.LocalDateTime> date = createDateTime("date", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> materialId = createNumber("materialId", Long.class);

    public final StringPath memo = createString("memo");

    public final NumberPath<java.math.BigDecimal> quantity = createNumber("quantity", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> stockAfter = createNumber("stockAfter", java.math.BigDecimal.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final StringPath type = createString("type");

    public final NumberPath<Long> unitPrice = createNumber("unitPrice", Long.class);

    public QInventoryLogView(String variable) {
        super(InventoryLogView.class, forVariable(variable));
    }

    public QInventoryLogView(Path<? extends InventoryLogView> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInventoryLogView(PathMetadata metadata) {
        super(InventoryLogView.class, metadata);
    }

}

