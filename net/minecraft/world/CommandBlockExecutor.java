package net.minecraft.world;

import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringHelper;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public abstract class CommandBlockExecutor implements CommandOutput {
   private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
   private static final Text DEFAULT_NAME = Text.literal("@");
   private long lastExecution = -1L;
   private boolean updateLastExecution = true;
   private int successCount;
   private boolean trackOutput = true;
   @Nullable
   private Text lastOutput;
   private String command = "";
   private Text customName;

   public CommandBlockExecutor() {
      this.customName = DEFAULT_NAME;
   }

   public int getSuccessCount() {
      return this.successCount;
   }

   public void setSuccessCount(int successCount) {
      this.successCount = successCount;
   }

   public Text getLastOutput() {
      return this.lastOutput == null ? ScreenTexts.EMPTY : this.lastOutput;
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      nbt.putString("Command", this.command);
      nbt.putInt("SuccessCount", this.successCount);
      nbt.putString("CustomName", Text.Serializer.toJson(this.customName));
      nbt.putBoolean("TrackOutput", this.trackOutput);
      if (this.lastOutput != null && this.trackOutput) {
         nbt.putString("LastOutput", Text.Serializer.toJson(this.lastOutput));
      }

      nbt.putBoolean("UpdateLastExecution", this.updateLastExecution);
      if (this.updateLastExecution && this.lastExecution > 0L) {
         nbt.putLong("LastExecution", this.lastExecution);
      }

      return nbt;
   }

   public void readNbt(NbtCompound nbt) {
      this.command = nbt.getString("Command");
      this.successCount = nbt.getInt("SuccessCount");
      if (nbt.contains("CustomName", NbtElement.STRING_TYPE)) {
         this.setCustomName(Text.Serializer.fromJson(nbt.getString("CustomName")));
      }

      if (nbt.contains("TrackOutput", NbtElement.BYTE_TYPE)) {
         this.trackOutput = nbt.getBoolean("TrackOutput");
      }

      if (nbt.contains("LastOutput", NbtElement.STRING_TYPE) && this.trackOutput) {
         try {
            this.lastOutput = Text.Serializer.fromJson(nbt.getString("LastOutput"));
         } catch (Throwable var3) {
            this.lastOutput = Text.literal(var3.getMessage());
         }
      } else {
         this.lastOutput = null;
      }

      if (nbt.contains("UpdateLastExecution")) {
         this.updateLastExecution = nbt.getBoolean("UpdateLastExecution");
      }

      if (this.updateLastExecution && nbt.contains("LastExecution")) {
         this.lastExecution = nbt.getLong("LastExecution");
      } else {
         this.lastExecution = -1L;
      }

   }

   public void setCommand(String command) {
      this.command = command;
      this.successCount = 0;
   }

   public String getCommand() {
      return this.command;
   }

   public boolean execute(World world) {
      if (!world.isClient && world.getTime() != this.lastExecution) {
         if ("Searge".equalsIgnoreCase(this.command)) {
            this.lastOutput = Text.literal("#itzlipofutzli");
            this.successCount = 1;
            return true;
         } else {
            this.successCount = 0;
            MinecraftServer minecraftServer = this.getWorld().getServer();
            if (minecraftServer.areCommandBlocksEnabled() && !StringHelper.isEmpty(this.command)) {
               try {
                  this.lastOutput = null;
                  ServerCommandSource lv = this.getSource().withConsumer((context, success, result) -> {
                     if (success) {
                        ++this.successCount;
                     }

                  });
                  minecraftServer.getCommandManager().executeWithPrefix(lv, this.command);
               } catch (Throwable var6) {
                  CrashReport lv2 = CrashReport.create(var6, "Executing command block");
                  CrashReportSection lv3 = lv2.addElement("Command to be executed");
                  lv3.add("Command", this::getCommand);
                  lv3.add("Name", () -> {
                     return this.getCustomName().getString();
                  });
                  throw new CrashException(lv2);
               }
            }

            if (this.updateLastExecution) {
               this.lastExecution = world.getTime();
            } else {
               this.lastExecution = -1L;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public Text getCustomName() {
      return this.customName;
   }

   public void setCustomName(@Nullable Text name) {
      if (name != null) {
         this.customName = name;
      } else {
         this.customName = DEFAULT_NAME;
      }

   }

   public void sendMessage(Text message) {
      if (this.trackOutput) {
         SimpleDateFormat var10001 = DATE_FORMAT;
         Date var10002 = new Date();
         this.lastOutput = Text.literal("[" + var10001.format(var10002) + "] ").append(message);
         this.markDirty();
      }

   }

   public abstract ServerWorld getWorld();

   public abstract void markDirty();

   public void setLastOutput(@Nullable Text lastOutput) {
      this.lastOutput = lastOutput;
   }

   public void setTrackOutput(boolean trackOutput) {
      this.trackOutput = trackOutput;
   }

   public boolean isTrackingOutput() {
      return this.trackOutput;
   }

   public ActionResult interact(PlayerEntity player) {
      if (!player.isCreativeLevelTwoOp()) {
         return ActionResult.PASS;
      } else {
         if (player.getEntityWorld().isClient) {
            player.openCommandBlockMinecartScreen(this);
         }

         return ActionResult.success(player.getWorld().isClient);
      }
   }

   public abstract Vec3d getPos();

   public abstract ServerCommandSource getSource();

   public boolean shouldReceiveFeedback() {
      return this.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK) && this.trackOutput;
   }

   public boolean shouldTrackOutput() {
      return this.trackOutput;
   }

   public boolean shouldBroadcastConsoleToOps() {
      return this.getWorld().getGameRules().getBoolean(GameRules.COMMAND_BLOCK_OUTPUT);
   }
}
