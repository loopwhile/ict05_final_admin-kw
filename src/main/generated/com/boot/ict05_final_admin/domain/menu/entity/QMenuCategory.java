package com.boot.ict05_final_admin.domain.menu.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMenuCategory is a Querydsl query type for MenuCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenuCategory extends EntityPathBase<MenuCategory> {

    private static final long serialVersionUID = 1288490220L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMenuCategory menuCategory = new QMenuCategory("menuCategory");

    public final NumberPath<Long> menuCategoryId = createNumber("menuCategoryId", Long.class);

    public final NumberPath<Short> menuCategoryLevel = createNumber("menuCategoryLevel", Short.class);

    public final StringPath menuCategoryName = createString("menuCategoryName");

    public final QMenuCategory menuCategoryParentId;

    public QMenuCategory(String variable) {
        this(MenuCategory.class, forVariable(variable), INITS);
    }

    public QMenuCategory(Path<? extends MenuCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMenuCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMenuCategory(PathMetadata metadata, PathInits inits) {
        this(MenuCategory.class, metadata, inits);
    }

    public QMenuCategory(Class<? extends MenuCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.menuCategoryParentId = inits.isInitialized("menuCategoryParentId") ? new QMenuCategory(forProperty("menuCategoryParentId"), inits.get("menuCategoryParentId")) : null;
    }

}

