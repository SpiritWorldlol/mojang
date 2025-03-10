package net.minecraft.client.resource.metadata;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.SpriteDimensions;

@Environment(EnvType.CLIENT)
public class AnimationResourceMetadata {
   public static final AnimationResourceMetadataReader READER = new AnimationResourceMetadataReader();
   public static final String KEY = "animation";
   public static final int EMPTY_FRAME_TIME = 1;
   public static final int UNDEFINED = -1;
   public static final AnimationResourceMetadata EMPTY = new AnimationResourceMetadata(Lists.newArrayList(), -1, -1, 1, false) {
      public SpriteDimensions getSize(int defaultWidth, int defaultHeight) {
         return new SpriteDimensions(defaultWidth, defaultHeight);
      }
   };
   private final List frames;
   private final int width;
   private final int height;
   private final int defaultFrameTime;
   private final boolean interpolate;

   public AnimationResourceMetadata(List frames, int width, int height, int defaultFrameTime, boolean interpolate) {
      this.frames = frames;
      this.width = width;
      this.height = height;
      this.defaultFrameTime = defaultFrameTime;
      this.interpolate = interpolate;
   }

   public SpriteDimensions getSize(int defaultWidth, int defaultHeight) {
      if (this.width != -1) {
         return this.height != -1 ? new SpriteDimensions(this.width, this.height) : new SpriteDimensions(this.width, defaultHeight);
      } else if (this.height != -1) {
         return new SpriteDimensions(defaultWidth, this.height);
      } else {
         int k = Math.min(defaultWidth, defaultHeight);
         return new SpriteDimensions(k, k);
      }
   }

   public int getDefaultFrameTime() {
      return this.defaultFrameTime;
   }

   public boolean shouldInterpolate() {
      return this.interpolate;
   }

   public void forEachFrame(FrameConsumer consumer) {
      Iterator var2 = this.frames.iterator();

      while(var2.hasNext()) {
         AnimationFrameResourceMetadata lv = (AnimationFrameResourceMetadata)var2.next();
         consumer.accept(lv.getIndex(), lv.getTime(this.defaultFrameTime));
      }

   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface FrameConsumer {
      void accept(int index, int frameTime);
   }
}
