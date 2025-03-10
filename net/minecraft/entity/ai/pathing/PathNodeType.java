package net.minecraft.entity.ai.pathing;

public enum PathNodeType {
   BLOCKED(-1.0F),
   OPEN(0.0F),
   WALKABLE(0.0F),
   WALKABLE_DOOR(0.0F),
   TRAPDOOR(0.0F),
   POWDER_SNOW(-1.0F),
   DANGER_POWDER_SNOW(0.0F),
   FENCE(-1.0F),
   LAVA(-1.0F),
   WATER(8.0F),
   WATER_BORDER(8.0F),
   RAIL(0.0F),
   UNPASSABLE_RAIL(-1.0F),
   DANGER_FIRE(8.0F),
   DAMAGE_FIRE(16.0F),
   DANGER_OTHER(8.0F),
   DAMAGE_OTHER(-1.0F),
   DOOR_OPEN(0.0F),
   DOOR_WOOD_CLOSED(-1.0F),
   DOOR_IRON_CLOSED(-1.0F),
   BREACH(4.0F),
   LEAVES(-1.0F),
   STICKY_HONEY(8.0F),
   COCOA(0.0F),
   DAMAGE_CAUTIOUS(0.0F);

   private final float defaultPenalty;

   private PathNodeType(float defaultPenalty) {
      this.defaultPenalty = defaultPenalty;
   }

   public float getDefaultPenalty() {
      return this.defaultPenalty;
   }

   // $FF: synthetic method
   private static PathNodeType[] method_36788() {
      return new PathNodeType[]{BLOCKED, OPEN, WALKABLE, WALKABLE_DOOR, TRAPDOOR, POWDER_SNOW, DANGER_POWDER_SNOW, FENCE, LAVA, WATER, WATER_BORDER, RAIL, UNPASSABLE_RAIL, DANGER_FIRE, DAMAGE_FIRE, DANGER_OTHER, DAMAGE_OTHER, DOOR_OPEN, DOOR_WOOD_CLOSED, DOOR_IRON_CLOSED, BREACH, LEAVES, STICKY_HONEY, COCOA, DAMAGE_CAUTIOUS};
   }
}
