package net.minecraft.loot;

import java.util.function.Consumer;
import net.minecraft.loot.context.LootContext;

public interface LootChoice {
   int getWeight(float luck);

   void generateLoot(Consumer lootConsumer, LootContext context);
}
