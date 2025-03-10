package net.minecraft.client.toast;

import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ToastManager {
   private static final int SPACES = 5;
   private static final int ALL_OCCUPIED = -1;
   final MinecraftClient client;
   private final List visibleEntries = new ArrayList();
   private final BitSet occupiedSpaces = new BitSet(5);
   private final Deque toastQueue = Queues.newArrayDeque();

   public ToastManager(MinecraftClient client) {
      this.client = client;
   }

   public void draw(DrawContext context) {
      if (!this.client.options.hudHidden) {
         int i = context.getScaledWindowWidth();
         this.visibleEntries.removeIf((visibleEntry) -> {
            if (visibleEntry != null && visibleEntry.draw(i, context)) {
               this.occupiedSpaces.clear(visibleEntry.topIndex, visibleEntry.topIndex + visibleEntry.requiredSpaceCount);
               return true;
            } else {
               return false;
            }
         });
         if (!this.toastQueue.isEmpty() && this.getEmptySpaceCount() > 0) {
            this.toastQueue.removeIf((toast) -> {
               int i = toast.getRequiredSpaceCount();
               int j = this.getTopIndex(i);
               if (j != -1) {
                  this.visibleEntries.add(new Entry(toast, j, i));
                  this.occupiedSpaces.set(j, j + i);
                  return true;
               } else {
                  return false;
               }
            });
         }

      }
   }

   private int getTopIndex(int requiredSpaces) {
      if (this.getEmptySpaceCount() >= requiredSpaces) {
         int j = 0;

         for(int k = 0; k < 5; ++k) {
            if (this.occupiedSpaces.get(k)) {
               j = 0;
            } else {
               ++j;
               if (j == requiredSpaces) {
                  return k + 1 - j;
               }
            }
         }
      }

      return -1;
   }

   private int getEmptySpaceCount() {
      return 5 - this.occupiedSpaces.cardinality();
   }

   @Nullable
   public Toast getToast(Class toastClass, Object type) {
      Iterator var3 = this.visibleEntries.iterator();

      Entry lv;
      do {
         if (!var3.hasNext()) {
            var3 = this.toastQueue.iterator();

            Toast lv2;
            do {
               if (!var3.hasNext()) {
                  return null;
               }

               lv2 = (Toast)var3.next();
            } while(!toastClass.isAssignableFrom(lv2.getClass()) || !lv2.getType().equals(type));

            return lv2;
         }

         lv = (Entry)var3.next();
      } while(lv == null || !toastClass.isAssignableFrom(lv.getInstance().getClass()) || !lv.getInstance().getType().equals(type));

      return lv.getInstance();
   }

   public void clear() {
      this.occupiedSpaces.clear();
      this.visibleEntries.clear();
      this.toastQueue.clear();
   }

   public void add(Toast toast) {
      this.toastQueue.add(toast);
   }

   public MinecraftClient getClient() {
      return this.client;
   }

   public double getNotificationDisplayTimeMultiplier() {
      return (Double)this.client.options.getNotificationDisplayTime().getValue();
   }

   @Environment(EnvType.CLIENT)
   private class Entry {
      private static final long DISAPPEAR_TIME = 600L;
      private final Toast instance;
      final int topIndex;
      final int requiredSpaceCount;
      private long startTime = -1L;
      private long showTime = -1L;
      private Toast.Visibility visibility;

      Entry(Toast instance, int topIndex, int requiredSpaceCount) {
         this.visibility = Toast.Visibility.SHOW;
         this.instance = instance;
         this.topIndex = topIndex;
         this.requiredSpaceCount = requiredSpaceCount;
      }

      public Toast getInstance() {
         return this.instance;
      }

      private float getDisappearProgress(long time) {
         float f = MathHelper.clamp((float)(time - this.startTime) / 600.0F, 0.0F, 1.0F);
         f *= f;
         return this.visibility == Toast.Visibility.HIDE ? 1.0F - f : f;
      }

      public boolean draw(int x, DrawContext context) {
         long l = Util.getMeasuringTimeMs();
         if (this.startTime == -1L) {
            this.startTime = l;
            this.visibility.playSound(ToastManager.this.client.getSoundManager());
         }

         if (this.visibility == Toast.Visibility.SHOW && l - this.startTime <= 600L) {
            this.showTime = l;
         }

         context.getMatrices().push();
         context.getMatrices().translate((float)x - (float)this.instance.getWidth() * this.getDisappearProgress(l), (float)(this.topIndex * 32), 800.0F);
         Toast.Visibility lv = this.instance.draw(context, ToastManager.this, l - this.showTime);
         context.getMatrices().pop();
         if (lv != this.visibility) {
            this.startTime = l - (long)((int)((1.0F - this.getDisappearProgress(l)) * 600.0F));
            this.visibility = lv;
            this.visibility.playSound(ToastManager.this.client.getSoundManager());
         }

         return this.visibility == Toast.Visibility.HIDE && l - this.startTime > 600L;
      }
   }
}
