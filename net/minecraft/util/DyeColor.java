package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import net.minecraft.block.MapColor;
import net.minecraft.util.function.ValueLists;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public enum DyeColor implements StringIdentifiable {
   WHITE(0, "white", 16383998, MapColor.WHITE, 15790320, 16777215),
   ORANGE(1, "orange", 16351261, MapColor.ORANGE, 15435844, 16738335),
   MAGENTA(2, "magenta", 13061821, MapColor.MAGENTA, 12801229, 16711935),
   LIGHT_BLUE(3, "light_blue", 3847130, MapColor.LIGHT_BLUE, 6719955, 10141901),
   YELLOW(4, "yellow", 16701501, MapColor.YELLOW, 14602026, 16776960),
   LIME(5, "lime", 8439583, MapColor.LIME, 4312372, 12582656),
   PINK(6, "pink", 15961002, MapColor.PINK, 14188952, 16738740),
   GRAY(7, "gray", 4673362, MapColor.GRAY, 4408131, 8421504),
   LIGHT_GRAY(8, "light_gray", 10329495, MapColor.LIGHT_GRAY, 11250603, 13882323),
   CYAN(9, "cyan", 1481884, MapColor.CYAN, 2651799, 65535),
   PURPLE(10, "purple", 8991416, MapColor.PURPLE, 8073150, 10494192),
   BLUE(11, "blue", 3949738, MapColor.BLUE, 2437522, 255),
   BROWN(12, "brown", 8606770, MapColor.BROWN, 5320730, 9127187),
   GREEN(13, "green", 6192150, MapColor.GREEN, 3887386, 65280),
   RED(14, "red", 11546150, MapColor.RED, 11743532, 16711680),
   BLACK(15, "black", 1908001, MapColor.BLACK, 1973019, 0);

   private static final IntFunction BY_ID = ValueLists.createIdToValueFunction(DyeColor::getId, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.ZERO);
   private static final Int2ObjectOpenHashMap BY_FIREWORK_COLOR = new Int2ObjectOpenHashMap((Map)Arrays.stream(values()).collect(Collectors.toMap((color) -> {
      return color.fireworkColor;
   }, (color) -> {
      return color;
   })));
   public static final StringIdentifiable.Codec CODEC = StringIdentifiable.createCodec(DyeColor::values);
   private final int id;
   private final String name;
   private final MapColor mapColor;
   private final float[] colorComponents;
   private final int fireworkColor;
   private final int signColor;

   private DyeColor(int id, String name, int color, MapColor mapColor, int fireworkColor, int signColor) {
      this.id = id;
      this.name = name;
      this.mapColor = mapColor;
      this.signColor = signColor;
      int n = (color & 16711680) >> 16;
      int o = (color & '\uff00') >> 8;
      int p = (color & 255) >> 0;
      this.colorComponents = new float[]{(float)n / 255.0F, (float)o / 255.0F, (float)p / 255.0F};
      this.fireworkColor = fireworkColor;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public float[] getColorComponents() {
      return this.colorComponents;
   }

   public MapColor getMapColor() {
      return this.mapColor;
   }

   public int getFireworkColor() {
      return this.fireworkColor;
   }

   public int getSignColor() {
      return this.signColor;
   }

   public static DyeColor byId(int id) {
      return (DyeColor)BY_ID.apply(id);
   }

   @Nullable
   @Contract("_,!null->!null;_,null->_")
   public static DyeColor byName(String name, @Nullable DyeColor defaultColor) {
      DyeColor lv = (DyeColor)CODEC.byId(name);
      return lv != null ? lv : defaultColor;
   }

   @Nullable
   public static DyeColor byFireworkColor(int color) {
      return (DyeColor)BY_FIREWORK_COLOR.get(color);
   }

   public String toString() {
      return this.name;
   }

   public String asString() {
      return this.name;
   }

   // $FF: synthetic method
   private static DyeColor[] method_36676() {
      return new DyeColor[]{WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY, LIGHT_GRAY, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK};
   }
}
