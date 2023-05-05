package net.minecraft.recipe;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface RecipeUnlocker {
   void setLastRecipe(@Nullable Recipe recipe);

   @Nullable
   Recipe getLastRecipe();

   default void unlockLastRecipe(PlayerEntity player, List ingredients) {
      Recipe lv = this.getLastRecipe();
      if (lv != null) {
         player.unlockCraftedRecipe(lv, ingredients);
         if (!lv.isIgnoredInRecipeBook()) {
            player.unlockRecipes((Collection)Collections.singleton(lv));
            this.setLastRecipe((Recipe)null);
         }
      }

   }

   default boolean shouldCraftRecipe(World world, ServerPlayerEntity player, Recipe recipe) {
      if (!recipe.isIgnoredInRecipeBook() && world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) && !player.getRecipeBook().contains(recipe)) {
         return false;
      } else {
         this.setLastRecipe(recipe);
         return true;
      }
   }
}
