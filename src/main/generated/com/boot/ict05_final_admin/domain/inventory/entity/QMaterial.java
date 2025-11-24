package com.boot.ict05_final_admin.domain.inventory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMaterial is a Querydsl query type for Material
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMaterial extends EntityPathBase<Material> {

    private static final long serialVersionUID = 1980031117L;

    public static final QMaterial material = new QMaterial("material");

    public final StringPath baseUnit = createString("baseUnit");

    public final StringPath code = createString("code");

    public final NumberPath<Integer> conversionRate = createNumber("conversionRate", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<MaterialCategory> materialCategory = createEnum("materialCategory", MaterialCategory.class);

    public final EnumPath<MaterialStatus> materialStatus = createEnum("materialStatus", MaterialStatus.class);

    public final EnumPath<MaterialTemperature> materialTemperature = createEnum("materialTemperature", MaterialTemperature.class);

    public final DateTimePath<java.time.LocalDateTime> modifyDate = createDateTime("modifyDate", java.time.LocalDateTime.class);

    public final StringPath name = createString("name");

    public final NumberPath<java.math.BigDecimal> optimalQuantity = createNumber("optimalQuantity", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> regDate = createDateTime("regDate", java.time.LocalDateTime.class);

    public final StringPath salesUnit = createString("salesUnit");

    public final StringPath supplier = createString("supplier");

    public QMaterial(String variable) {
        super(Material.class, forVariable(variable));
    }

    public QMaterial(Path<? extends Material> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMaterial(PathMetadata metadata) {
        super(Material.class, metadata);
    }

}

