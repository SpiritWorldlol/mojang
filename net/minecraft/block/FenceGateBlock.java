package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

public class FenceGateBlock extends HorizontalFacingBlock {
   public static final BooleanProperty OPEN;
   public static final BooleanProperty POWERED;
   public static final BooleanProperty IN_WALL;
   protected static final VoxelShape Z_AXIS_SHAPE;
   protected static final VoxelShape X_AXIS_SHAPE;
   protected static final VoxelShape IN_WALL_Z_AXIS_SHAPE;
   protected static final VoxelShape IN_WALL_X_AXIS_SHAPE;
   protected static final VoxelShape Z_AXIS_COLLISION_SHAPE;
   protected static final VoxelShape X_AXIS_COLLISION_SHAPE;
   protected static final VoxelShape Z_AXIS_SIDES_SHAPE;
   protected static final VoxelShape X_AXIS_SIDES_SHAPE;
   protected static final VoxelShape Z_AXIS_CULL_SHAPE;
   protected static final VoxelShape X_AXIS_CULL_SHAPE;
   protected static final VoxelShape IN_WALL_Z_AXIS_CULL_SHAPE;
   protected static final VoxelShape IN_WALL_X_AXIS_CULL_SHAPE;
   private final WoodType type;

   public FenceGateBlock(AbstractBlock.Settings settings, WoodType type) {
      super(settings.sounds(type.soundType()));
      this.type = type;
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(OPEN, false)).with(POWERED, false)).with(IN_WALL, false));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      if ((Boolean)state.get(IN_WALL)) {
         return ((Direction)state.get(FACING)).getAxis() == Direction.Axis.X ? IN_WALL_X_AXIS_SHAPE : IN_WALL_Z_AXIS_SHAPE;
      } else {
         return ((Direction)state.get(FACING)).getAxis() == Direction.Axis.X ? X_AXIS_SHAPE : Z_AXIS_SHAPE;
      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      Direction.Axis lv = direction.getAxis();
      if (((Direction)state.get(FACING)).rotateYClockwise().getAxis() != lv) {
         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      } else {
         boolean bl = this.isWall(neighborState) || this.isWall(world.getBlockState(pos.offset(direction.getOpposite())));
         return (BlockState)state.with(IN_WALL, bl);
      }
   }

   public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
      if ((Boolean)state.get(OPEN)) {
         return VoxelShapes.empty();
      } else {
         return ((Direction)state.get(FACING)).getAxis() == Direction.Axis.Z ? Z_AXIS_SIDES_SHAPE : X_AXIS_SIDES_SHAPE;
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      if ((Boolean)state.get(OPEN)) {
         return VoxelShapes.empty();
      } else {
         return ((Direction)state.get(FACING)).getAxis() == Direction.Axis.Z ? Z_AXIS_COLLISION_SHAPE : X_AXIS_COLLISION_SHAPE;
      }
   }

   public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
      if ((Boolean)state.get(IN_WALL)) {
         return ((Direction)state.get(FACING)).getAxis() == Direction.Axis.X ? IN_WALL_X_AXIS_CULL_SHAPE : IN_WALL_Z_AXIS_CULL_SHAPE;
      } else {
         return ((Direction)state.get(FACING)).getAxis() == Direction.Axis.X ? X_AXIS_CULL_SHAPE : Z_AXIS_CULL_SHAPE;
      }
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      switch (type) {
         case LAND:
            return (Boolean)state.get(OPEN);
         case WATER:
            return false;
         case AIR:
            return (Boolean)state.get(OPEN);
         default:
            return false;
      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      World lv = ctx.getWorld();
      BlockPos lv2 = ctx.getBlockPos();
      boolean bl = lv.isReceivingRedstonePower(lv2);
      Direction lv3 = ctx.getHorizontalPlayerFacing();
      Direction.Axis lv4 = lv3.getAxis();
      boolean bl2 = lv4 == Direction.Axis.Z && (this.isWall(lv.getBlockState(lv2.west())) || this.isWall(lv.getBlockState(lv2.east()))) || lv4 == Direction.Axis.X && (this.isWall(lv.getBlockState(lv2.north())) || this.isWall(lv.getBlockState(lv2.south())));
      return (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, lv3)).with(OPEN, bl)).with(POWERED, bl)).with(IN_WALL, bl2);
   }

   private boolean isWall(BlockState state) {
      return state.isIn(BlockTags.WALLS);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if ((Boolean)state.get(OPEN)) {
         state = (BlockState)state.with(OPEN, false);
         world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
      } else {
         Direction lv = player.getHorizontalFacing();
         if (state.get(FACING) == lv.getOpposite()) {
            state = (BlockState)state.with(FACING, lv);
         }

         state = (BlockState)state.with(OPEN, true);
         world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
      }

      boolean bl = (Boolean)state.get(OPEN);
      world.playSound(player, pos, bl ? this.type.fenceGateOpen() : this.type.fenceGateClose(), SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F);
      world.emitGameEvent(player, bl ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
      return ActionResult.success(world.isClient);
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      if (!world.isClient) {
         boolean bl2 = world.isReceivingRedstonePower(pos);
         if ((Boolean)state.get(POWERED) != bl2) {
            world.setBlockState(pos, (BlockState)((BlockState)state.with(POWERED, bl2)).with(OPEN, bl2), Block.NOTIFY_LISTENERS);
            if ((Boolean)state.get(OPEN) != bl2) {
               world.playSound((PlayerEntity)null, pos, bl2 ? this.type.fenceGateOpen() : this.type.fenceGateClose(), SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F);
               world.emitGameEvent((Entity)null, bl2 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }
         }

      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, OPEN, POWERED, IN_WALL);
   }

   public static boolean canWallConnect(BlockState state, Direction side) {
      return ((Direction)state.get(FACING)).getAxis() == side.rotateYClockwise().getAxis();
   }

   static {
      OPEN = Properties.OPEN;
      POWERED = Properties.POWERED;
      IN_WALL = Properties.IN_WALL;
      Z_AXIS_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
      X_AXIS_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
      IN_WALL_Z_AXIS_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 13.0, 10.0);
      IN_WALL_X_AXIS_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 13.0, 16.0);
      Z_AXIS_COLLISION_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 24.0, 10.0);
      X_AXIS_COLLISION_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 24.0, 16.0);
      Z_AXIS_SIDES_SHAPE = Block.createCuboidShape(0.0, 5.0, 6.0, 16.0, 24.0, 10.0);
      X_AXIS_SIDES_SHAPE = Block.createCuboidShape(6.0, 5.0, 0.0, 10.0, 24.0, 16.0);
      Z_AXIS_CULL_SHAPE = VoxelShapes.union(Block.createCuboidShape(0.0, 5.0, 7.0, 2.0, 16.0, 9.0), Block.createCuboidShape(14.0, 5.0, 7.0, 16.0, 16.0, 9.0));
      X_AXIS_CULL_SHAPE = VoxelShapes.union(Block.createCuboidShape(7.0, 5.0, 0.0, 9.0, 16.0, 2.0), Block.createCuboidShape(7.0, 5.0, 14.0, 9.0, 16.0, 16.0));
      IN_WALL_Z_AXIS_CULL_SHAPE = VoxelShapes.union(Block.createCuboidShape(0.0, 2.0, 7.0, 2.0, 13.0, 9.0), Block.createCuboidShape(14.0, 2.0, 7.0, 16.0, 13.0, 9.0));
      IN_WALL_X_AXIS_CULL_SHAPE = VoxelShapes.union(Block.createCuboidShape(7.0, 2.0, 0.0, 9.0, 13.0, 2.0), Block.createCuboidShape(7.0, 2.0, 14.0, 9.0, 13.0, 16.0));
   }
}
