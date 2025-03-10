package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.enums.RailShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RailPlacementHelper {
   private final World world;
   private final BlockPos pos;
   private final AbstractRailBlock block;
   private BlockState state;
   private final boolean forbidCurves;
   private final List neighbors = Lists.newArrayList();

   public RailPlacementHelper(World world, BlockPos pos, BlockState state) {
      this.world = world;
      this.pos = pos;
      this.state = state;
      this.block = (AbstractRailBlock)state.getBlock();
      RailShape lv = (RailShape)state.get(this.block.getShapeProperty());
      this.forbidCurves = this.block.cannotMakeCurves();
      this.computeNeighbors(lv);
   }

   public List getNeighbors() {
      return this.neighbors;
   }

   private void computeNeighbors(RailShape shape) {
      this.neighbors.clear();
      switch (shape) {
         case NORTH_SOUTH:
            this.neighbors.add(this.pos.north());
            this.neighbors.add(this.pos.south());
            break;
         case EAST_WEST:
            this.neighbors.add(this.pos.west());
            this.neighbors.add(this.pos.east());
            break;
         case ASCENDING_EAST:
            this.neighbors.add(this.pos.west());
            this.neighbors.add(this.pos.east().up());
            break;
         case ASCENDING_WEST:
            this.neighbors.add(this.pos.west().up());
            this.neighbors.add(this.pos.east());
            break;
         case ASCENDING_NORTH:
            this.neighbors.add(this.pos.north().up());
            this.neighbors.add(this.pos.south());
            break;
         case ASCENDING_SOUTH:
            this.neighbors.add(this.pos.north());
            this.neighbors.add(this.pos.south().up());
            break;
         case SOUTH_EAST:
            this.neighbors.add(this.pos.east());
            this.neighbors.add(this.pos.south());
            break;
         case SOUTH_WEST:
            this.neighbors.add(this.pos.west());
            this.neighbors.add(this.pos.south());
            break;
         case NORTH_WEST:
            this.neighbors.add(this.pos.west());
            this.neighbors.add(this.pos.north());
            break;
         case NORTH_EAST:
            this.neighbors.add(this.pos.east());
            this.neighbors.add(this.pos.north());
      }

   }

   private void updateNeighborPositions() {
      for(int i = 0; i < this.neighbors.size(); ++i) {
         RailPlacementHelper lv = this.getNeighboringRail((BlockPos)this.neighbors.get(i));
         if (lv != null && lv.isNeighbor(this)) {
            this.neighbors.set(i, lv.pos);
         } else {
            this.neighbors.remove(i--);
         }
      }

   }

   private boolean isVerticallyNearRail(BlockPos pos) {
      return AbstractRailBlock.isRail(this.world, pos) || AbstractRailBlock.isRail(this.world, pos.up()) || AbstractRailBlock.isRail(this.world, pos.down());
   }

   @Nullable
   private RailPlacementHelper getNeighboringRail(BlockPos pos) {
      BlockState lv2 = this.world.getBlockState(pos);
      if (AbstractRailBlock.isRail(lv2)) {
         return new RailPlacementHelper(this.world, pos, lv2);
      } else {
         BlockPos lv = pos.up();
         lv2 = this.world.getBlockState(lv);
         if (AbstractRailBlock.isRail(lv2)) {
            return new RailPlacementHelper(this.world, lv, lv2);
         } else {
            lv = pos.down();
            lv2 = this.world.getBlockState(lv);
            return AbstractRailBlock.isRail(lv2) ? new RailPlacementHelper(this.world, lv, lv2) : null;
         }
      }
   }

   private boolean isNeighbor(RailPlacementHelper other) {
      return this.isNeighbor(other.pos);
   }

   private boolean isNeighbor(BlockPos pos) {
      for(int i = 0; i < this.neighbors.size(); ++i) {
         BlockPos lv = (BlockPos)this.neighbors.get(i);
         if (lv.getX() == pos.getX() && lv.getZ() == pos.getZ()) {
            return true;
         }
      }

      return false;
   }

   protected int getNeighborCount() {
      int i = 0;
      Iterator var2 = Direction.Type.HORIZONTAL.iterator();

      while(var2.hasNext()) {
         Direction lv = (Direction)var2.next();
         if (this.isVerticallyNearRail(this.pos.offset(lv))) {
            ++i;
         }
      }

      return i;
   }

   private boolean canConnect(RailPlacementHelper placementHelper) {
      return this.isNeighbor(placementHelper) || this.neighbors.size() != 2;
   }

   private void computeRailShape(RailPlacementHelper placementHelper) {
      this.neighbors.add(placementHelper.pos);
      BlockPos lv = this.pos.north();
      BlockPos lv2 = this.pos.south();
      BlockPos lv3 = this.pos.west();
      BlockPos lv4 = this.pos.east();
      boolean bl = this.isNeighbor(lv);
      boolean bl2 = this.isNeighbor(lv2);
      boolean bl3 = this.isNeighbor(lv3);
      boolean bl4 = this.isNeighbor(lv4);
      RailShape lv5 = null;
      if (bl || bl2) {
         lv5 = RailShape.NORTH_SOUTH;
      }

      if (bl3 || bl4) {
         lv5 = RailShape.EAST_WEST;
      }

      if (!this.forbidCurves) {
         if (bl2 && bl4 && !bl && !bl3) {
            lv5 = RailShape.SOUTH_EAST;
         }

         if (bl2 && bl3 && !bl && !bl4) {
            lv5 = RailShape.SOUTH_WEST;
         }

         if (bl && bl3 && !bl2 && !bl4) {
            lv5 = RailShape.NORTH_WEST;
         }

         if (bl && bl4 && !bl2 && !bl3) {
            lv5 = RailShape.NORTH_EAST;
         }
      }

      if (lv5 == RailShape.NORTH_SOUTH) {
         if (AbstractRailBlock.isRail(this.world, lv.up())) {
            lv5 = RailShape.ASCENDING_NORTH;
         }

         if (AbstractRailBlock.isRail(this.world, lv2.up())) {
            lv5 = RailShape.ASCENDING_SOUTH;
         }
      }

      if (lv5 == RailShape.EAST_WEST) {
         if (AbstractRailBlock.isRail(this.world, lv4.up())) {
            lv5 = RailShape.ASCENDING_EAST;
         }

         if (AbstractRailBlock.isRail(this.world, lv3.up())) {
            lv5 = RailShape.ASCENDING_WEST;
         }
      }

      if (lv5 == null) {
         lv5 = RailShape.NORTH_SOUTH;
      }

      this.state = (BlockState)this.state.with(this.block.getShapeProperty(), lv5);
      this.world.setBlockState(this.pos, this.state, Block.NOTIFY_ALL);
   }

   private boolean canConnect(BlockPos pos) {
      RailPlacementHelper lv = this.getNeighboringRail(pos);
      if (lv == null) {
         return false;
      } else {
         lv.updateNeighborPositions();
         return lv.canConnect(this);
      }
   }

   public RailPlacementHelper updateBlockState(boolean powered, boolean forceUpdate, RailShape railShape) {
      BlockPos lv = this.pos.north();
      BlockPos lv2 = this.pos.south();
      BlockPos lv3 = this.pos.west();
      BlockPos lv4 = this.pos.east();
      boolean bl3 = this.canConnect(lv);
      boolean bl4 = this.canConnect(lv2);
      boolean bl5 = this.canConnect(lv3);
      boolean bl6 = this.canConnect(lv4);
      RailShape lv5 = null;
      boolean bl7 = bl3 || bl4;
      boolean bl8 = bl5 || bl6;
      if (bl7 && !bl8) {
         lv5 = RailShape.NORTH_SOUTH;
      }

      if (bl8 && !bl7) {
         lv5 = RailShape.EAST_WEST;
      }

      boolean bl9 = bl4 && bl6;
      boolean bl10 = bl4 && bl5;
      boolean bl11 = bl3 && bl6;
      boolean bl12 = bl3 && bl5;
      if (!this.forbidCurves) {
         if (bl9 && !bl3 && !bl5) {
            lv5 = RailShape.SOUTH_EAST;
         }

         if (bl10 && !bl3 && !bl6) {
            lv5 = RailShape.SOUTH_WEST;
         }

         if (bl12 && !bl4 && !bl6) {
            lv5 = RailShape.NORTH_WEST;
         }

         if (bl11 && !bl4 && !bl5) {
            lv5 = RailShape.NORTH_EAST;
         }
      }

      if (lv5 == null) {
         if (bl7 && bl8) {
            lv5 = railShape;
         } else if (bl7) {
            lv5 = RailShape.NORTH_SOUTH;
         } else if (bl8) {
            lv5 = RailShape.EAST_WEST;
         }

         if (!this.forbidCurves) {
            if (powered) {
               if (bl9) {
                  lv5 = RailShape.SOUTH_EAST;
               }

               if (bl10) {
                  lv5 = RailShape.SOUTH_WEST;
               }

               if (bl11) {
                  lv5 = RailShape.NORTH_EAST;
               }

               if (bl12) {
                  lv5 = RailShape.NORTH_WEST;
               }
            } else {
               if (bl12) {
                  lv5 = RailShape.NORTH_WEST;
               }

               if (bl11) {
                  lv5 = RailShape.NORTH_EAST;
               }

               if (bl10) {
                  lv5 = RailShape.SOUTH_WEST;
               }

               if (bl9) {
                  lv5 = RailShape.SOUTH_EAST;
               }
            }
         }
      }

      if (lv5 == RailShape.NORTH_SOUTH) {
         if (AbstractRailBlock.isRail(this.world, lv.up())) {
            lv5 = RailShape.ASCENDING_NORTH;
         }

         if (AbstractRailBlock.isRail(this.world, lv2.up())) {
            lv5 = RailShape.ASCENDING_SOUTH;
         }
      }

      if (lv5 == RailShape.EAST_WEST) {
         if (AbstractRailBlock.isRail(this.world, lv4.up())) {
            lv5 = RailShape.ASCENDING_EAST;
         }

         if (AbstractRailBlock.isRail(this.world, lv3.up())) {
            lv5 = RailShape.ASCENDING_WEST;
         }
      }

      if (lv5 == null) {
         lv5 = railShape;
      }

      this.computeNeighbors(lv5);
      this.state = (BlockState)this.state.with(this.block.getShapeProperty(), lv5);
      if (forceUpdate || this.world.getBlockState(this.pos) != this.state) {
         this.world.setBlockState(this.pos, this.state, Block.NOTIFY_ALL);

         for(int i = 0; i < this.neighbors.size(); ++i) {
            RailPlacementHelper lv6 = this.getNeighboringRail((BlockPos)this.neighbors.get(i));
            if (lv6 != null) {
               lv6.updateNeighborPositions();
               if (lv6.canConnect(this)) {
                  lv6.computeRailShape(this);
               }
            }
         }
      }

      return this;
   }

   public BlockState getBlockState() {
      return this.state;
   }
}
