package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RecipeBookGhostSlots {
   @Nullable
   private Recipe recipe;
   private final List slots = Lists.newArrayList();
   float time;

   public void reset() {
      this.recipe = null;
      this.slots.clear();
      this.time = 0.0F;
   }

   public void addSlot(Ingredient ingredient, int x, int y) {
      this.slots.add(new GhostInputSlot(ingredient, x, y));
   }

   public GhostInputSlot getSlot(int index) {
      return (GhostInputSlot)this.slots.get(index);
   }

   public int getSlotCount() {
      return this.slots.size();
   }

   @Nullable
   public Recipe getRecipe() {
      return this.recipe;
   }

   public void setRecipe(Recipe recipe) {
      this.recipe = recipe;
   }

   public void draw(DrawContext context, MinecraftClient client, int x, int y, boolean notInventory, float tickDelta) {
      if (!Screen.hasControlDown()) {
         this.time += tickDelta;
      }

      for(int k = 0; k < this.slots.size(); ++k) {
         GhostInputSlot lv = (GhostInputSlot)this.slots.get(k);
         int l = lv.getX() + x;
         int m = lv.getY() + y;
         if (k == 0 && notInventory) {
            context.fill(l - 4, m - 4, l + 20, m + 20, 822018048);
         } else {
            context.fill(l, m, l + 16, m + 16, 822018048);
         }

         ItemStack lv2 = lv.getCurrentItemStack();
         context.drawItemWithoutEntity(lv2, l, m);
         context.method_51739(RenderLayer.method_51787(), l, m, l + 16, m + 16, 822083583);
         if (k == 0) {
            context.drawItemInSlot(client.textRenderer, lv2, l, m);
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public class GhostInputSlot {
      private final Ingredient ingredient;
      private final int x;
      private final int y;

      public GhostInputSlot(Ingredient ingredient, int x, int y) {
         this.ingredient = ingredient;
         this.x = x;
         this.y = y;
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }

      public ItemStack getCurrentItemStack() {
         ItemStack[] lvs = this.ingredient.getMatchingStacks();
         return lvs.length == 0 ? ItemStack.EMPTY : lvs[MathHelper.floor(RecipeBookGhostSlots.this.time / 30.0F) % lvs.length];
      }
   }
}
