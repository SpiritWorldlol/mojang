package net.minecraft.recipe.book;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class RecipeBook {
   protected final Set recipes = Sets.newHashSet();
   protected final Set toBeDisplayed = Sets.newHashSet();
   private final RecipeBookOptions options = new RecipeBookOptions();

   public void copyFrom(RecipeBook book) {
      this.recipes.clear();
      this.toBeDisplayed.clear();
      this.options.copyFrom(book.options);
      this.recipes.addAll(book.recipes);
      this.toBeDisplayed.addAll(book.toBeDisplayed);
   }

   public void add(Recipe recipe) {
      if (!recipe.isIgnoredInRecipeBook()) {
         this.add(recipe.getId());
      }

   }

   protected void add(Identifier id) {
      this.recipes.add(id);
   }

   public boolean contains(@Nullable Recipe recipe) {
      return recipe == null ? false : this.recipes.contains(recipe.getId());
   }

   public boolean contains(Identifier id) {
      return this.recipes.contains(id);
   }

   public void remove(Recipe recipe) {
      this.remove(recipe.getId());
   }

   protected void remove(Identifier id) {
      this.recipes.remove(id);
      this.toBeDisplayed.remove(id);
   }

   public boolean shouldDisplay(Recipe recipe) {
      return this.toBeDisplayed.contains(recipe.getId());
   }

   public void onRecipeDisplayed(Recipe recipe) {
      this.toBeDisplayed.remove(recipe.getId());
   }

   public void display(Recipe recipe) {
      this.display(recipe.getId());
   }

   protected void display(Identifier id) {
      this.toBeDisplayed.add(id);
   }

   public boolean isGuiOpen(RecipeBookCategory category) {
      return this.options.isGuiOpen(category);
   }

   public void setGuiOpen(RecipeBookCategory category, boolean open) {
      this.options.setGuiOpen(category, open);
   }

   public boolean isFilteringCraftable(AbstractRecipeScreenHandler handler) {
      return this.isFilteringCraftable(handler.getCategory());
   }

   public boolean isFilteringCraftable(RecipeBookCategory category) {
      return this.options.isFilteringCraftable(category);
   }

   public void setFilteringCraftable(RecipeBookCategory category, boolean filteringCraftable) {
      this.options.setFilteringCraftable(category, filteringCraftable);
   }

   public void setOptions(RecipeBookOptions options) {
      this.options.copyFrom(options);
   }

   public RecipeBookOptions getOptions() {
      return this.options.copy();
   }

   public void setCategoryOptions(RecipeBookCategory category, boolean guiOpen, boolean filteringCraftable) {
      this.options.setGuiOpen(category, guiOpen);
      this.options.setFilteringCraftable(category, filteringCraftable);
   }
}
