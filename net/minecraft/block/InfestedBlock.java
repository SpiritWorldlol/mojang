package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;

public class InfestedBlock extends Block {
   private final Block regularBlock;
   private static final Map REGULAR_TO_INFESTED_BLOCK = Maps.newIdentityHashMap();
   private static final Map REGULAR_TO_INFESTED_STATE = Maps.newIdentityHashMap();
   private static final Map INFESTED_TO_REGULAR_STATE = Maps.newIdentityHashMap();

   public InfestedBlock(Block regularBlock, AbstractBlock.Settings settings) {
      super(settings.hardness(regularBlock.getHardness() / 2.0F).resistance(0.75F));
      this.regularBlock = regularBlock;
      REGULAR_TO_INFESTED_BLOCK.put(regularBlock, this);
   }

   public Block getRegularBlock() {
      return this.regularBlock;
   }

   public static boolean isInfestable(BlockState block) {
      return REGULAR_TO_INFESTED_BLOCK.containsKey(block.getBlock());
   }

   private void spawnSilverfish(ServerWorld world, BlockPos pos) {
      SilverfishEntity lv = (SilverfishEntity)EntityType.SILVERFISH.create(world);
      if (lv != null) {
         lv.refreshPositionAndAngles((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5, 0.0F, 0.0F);
         world.spawnEntity(lv);
         lv.playSpawnEffects();
      }

   }

   public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
      super.onStacksDropped(state, world, pos, tool, dropExperience);
      if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS) && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) == 0) {
         this.spawnSilverfish(world, pos);
      }

   }

   public static BlockState fromRegularState(BlockState regularState) {
      return copyProperties(REGULAR_TO_INFESTED_STATE, regularState, () -> {
         return ((Block)REGULAR_TO_INFESTED_BLOCK.get(regularState.getBlock())).getDefaultState();
      });
   }

   public BlockState toRegularState(BlockState infestedState) {
      return copyProperties(INFESTED_TO_REGULAR_STATE, infestedState, () -> {
         return this.getRegularBlock().getDefaultState();
      });
   }

   private static BlockState copyProperties(Map stateMap, BlockState fromState, Supplier toStateSupplier) {
      return (BlockState)stateMap.computeIfAbsent(fromState, (infestedState) -> {
         BlockState lv = (BlockState)toStateSupplier.get();

         Property lv2;
         for(Iterator var3 = infestedState.getProperties().iterator(); var3.hasNext(); lv = lv.contains(lv2) ? (BlockState)lv.with(lv2, infestedState.get(lv2)) : lv) {
            lv2 = (Property)var3.next();
         }

         return lv;
      });
   }
}
