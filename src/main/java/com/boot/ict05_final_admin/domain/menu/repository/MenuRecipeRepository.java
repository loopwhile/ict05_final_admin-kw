package com.boot.ict05_final_admin.domain.menu.repository;

import com.boot.ict05_final_admin.domain.menu.entity.Menu;
import com.boot.ict05_final_admin.domain.menu.entity.MenuRecipe;
import com.boot.ict05_final_admin.domain.menu.entity.MenuRecipe.RecipeRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRecipeRepository extends JpaRepository<MenuRecipe, Long> {
    void deleteAllByMenu(Menu menu);
}
