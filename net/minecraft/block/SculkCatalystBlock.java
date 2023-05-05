package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SculkCatalystBlock extends BlockWithEntity {
   public static final BooleanProperty BLOOM;
   private final IntProvider experience = ConstantIntProvider.create(5);

   public SculkCatalystBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(BLOOM, false));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(BLOOM);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(BLOOM)) {
         world.setBlockState(pos, (BlockState)state.with(BLOOM, false), Block.NOTIFY_ALL);
      }

   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new SculkCatalystBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return world.isClient ? null : checkType(type, BlockEntityType.SCULK_CATALYST, SculkCatalystBlockEntity::tick);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
      super.onStacksDropped(state, world, pos, tool, dropExperience);
      if (dropExperience) {
         this.dropExperienceWhenMined(world, pos, tool, this.experience);
      }

   }

   static {
      BLOOM = Properties.BLOOM;
   }
}
