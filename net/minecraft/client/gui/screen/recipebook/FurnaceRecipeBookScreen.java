package net.minecraft.client.gui.screen.recipebook;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class FurnaceRecipeBookScreen extends AbstractFurnaceRecipeBookScreen {
   private static final Text TOGGLE_SMELTABLE_RECIPES_TEXT = Text.translatable("gui.recipebook.toggleRecipes.smeltable");

   protected Text getToggleCraftableButtonText() {
      return TOGGLE_SMELTABLE_RECIPES_TEXT;
   }

   protected Set getAllowedFuels() {
      return AbstractFurnaceBlockEntity.createFuelTimeMap().keySet();
   }
}
