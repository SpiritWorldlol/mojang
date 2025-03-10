package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class WeightedPressurePlateBlock extends AbstractPressurePlateBlock {
   public static final IntProperty POWER;
   private final int weight;

   protected WeightedPressurePlateBlock(int weight, AbstractBlock.Settings settings, BlockSetType blockSetType) {
      super(settings, blockSetType);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWER, 0));
      this.weight = weight;
   }

   protected int getRedstoneOutput(World world, BlockPos pos) {
      int i = Math.min(world.getNonSpectatingEntities(Entity.class, BOX.offset(pos)).size(), this.weight);
      if (i > 0) {
         float f = (float)Math.min(this.weight, i) / (float)this.weight;
         return MathHelper.ceil(f * 15.0F);
      } else {
         return 0;
      }
   }

   protected int getRedstoneOutput(BlockState state) {
      return (Integer)state.get(POWER);
   }

   protected BlockState setRedstoneOutput(BlockState state, int rsOut) {
      return (BlockState)state.with(POWER, rsOut);
   }

   protected int getTickRate() {
      return 10;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(POWER);
   }

   static {
      POWER = Properties.POWER;
   }
}
