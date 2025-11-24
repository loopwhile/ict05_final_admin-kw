package com.boot.ict05_final_admin.domain.menu.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMenu is a Querydsl query type for Menu
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenu extends EntityPathBase<Menu> {

    private static final long serialVersionUID = 1665620430L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMenu menu = new QMenu("menu");

    public final QMenuCategory menuCategory;

    public final StringPath menuCode = createString("menuCode");

    public final NumberPath<Long> menuId = createNumber("menuId", Long.class);

    public final StringPath menuInformation = createString("menuInformation");

    public final NumberPath<Integer> menuKcal = createNumber("menuKcal", Integer.class);

    public final StringPath menuName = createString("menuName");

    public final StringPath menuNameEnglish = createString("menuNameEnglish");

    public final NumberPath<java.math.BigDecimal> menuPrice = createNumber("menuPrice", java.math.BigDecimal.class);

    public final EnumPath<MenuShow> menuShow = createEnum("menuShow", MenuShow.class);

    public final ListPath<MenuRecipe, QMenuRecipe> recipe = this.<MenuRecipe, QMenuRecipe>createList("recipe", MenuRecipe.class, QMenuRecipe.class, PathInits.DIRECT2);

    public QMenu(String variable) {
        this(Menu.class, forVariable(variable), INITS);
    }

    public QMenu(Path<? extends Menu> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMenu(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMenu(PathMetadata metadata, PathInits inits) {
        this(Menu.class, metadata, inits);
    }

    public QMenu(Class<? extends Menu> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.menuCategory = inits.isInitialized("menuCategory") ? new QMenuCategory(forProperty("menuCategory"), inits.get("menuCategory")) : null;
    }

}

