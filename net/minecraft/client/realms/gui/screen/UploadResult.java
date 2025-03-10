package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class UploadResult {
   public final int statusCode;
   @Nullable
   public final String errorMessage;

   UploadResult(int statusCode, String errorMessage) {
      this.statusCode = statusCode;
      this.errorMessage = errorMessage;
   }

   @Environment(EnvType.CLIENT)
   public static class Builder {
      private int statusCode = -1;
      private String errorMessage;

      public Builder withStatusCode(int statusCode) {
         this.statusCode = statusCode;
         return this;
      }

      public Builder withErrorMessage(@Nullable String errorMessage) {
         this.errorMessage = errorMessage;
         return this;
      }

      public UploadResult build() {
         return new UploadResult(this.statusCode, this.errorMessage);
      }
   }
}
