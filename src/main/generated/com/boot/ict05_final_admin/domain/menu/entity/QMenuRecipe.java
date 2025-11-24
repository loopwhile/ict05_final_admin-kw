package com.boot.ict05_final_admin.domain.menu.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMenuRecipe is a Querydsl query type for MenuRecipe
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenuRecipe extends EntityPathBase<MenuRecipe> {

    private static final long serialVersionUID = -1081112324L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMenuRecipe menuRecipe = new QMenuRecipe("menuRecipe");

    public final com.boot.ict05_final_admin.domain.inventory.entity.QMaterial material;

    public final QMenu menu;

    public final NumberPath<Long> menuRecipeId = createNumber("menuRecipeId", Long.class);

    public final StringPath recipeItemName = createString("recipeItemName");

    public final NumberPath<java.math.BigDecimal> recipeQty = createNumber("recipeQty", java.math.BigDecimal.class);

    public final EnumPath<MenuRecipe.RecipeRole> recipeRole = createEnum("recipeRole", MenuRecipe.RecipeRole.class);

    public final NumberPath<Integer> recipeSort = createNumber("recipeSort", Integer.class);

    public final EnumPath<RecipeUnit> recipeUnit = createEnum("recipeUnit", RecipeUnit.class);

    public QMenuRecipe(String variable) {
        this(MenuRecipe.class, forVariable(variable), INITS);
    }

    public QMenuRecipe(Path<? extends MenuRecipe> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMenuRecipe(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMenuRecipe(PathMetadata metadata, PathInits inits) {
        this(MenuRecipe.class, metadata, inits);
    }

    public QMenuRecipe(Class<? extends MenuRecipe> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.material = inits.isInitialized("material") ? new com.boot.ict05_final_admin.domain.inventory.entity.QMaterial(forProperty("material")) : null;
        this.menu = inits.isInitialized("menu") ? new QMenu(forProperty("menu"), inits.get("menu")) : null;
    }

}

