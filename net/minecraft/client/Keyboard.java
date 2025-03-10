package net.minecraft.client;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.TextureUtil;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.screen.option.SimpleOptionsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WinNativeModuleUtil;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class Keyboard {
   public static final int DEBUG_CRASH_TIME = 10000;
   private final MinecraftClient client;
   private final Clipboard clipboard = new Clipboard();
   private long debugCrashStartTime = -1L;
   private long debugCrashLastLogTime = -1L;
   private long debugCrashElapsedTime = -1L;
   private boolean switchF3State;

   public Keyboard(MinecraftClient client) {
      this.client = client;
   }

   private boolean processDebugKeys(int key) {
      switch (key) {
         case 69:
            this.client.debugChunkInfo = !this.client.debugChunkInfo;
            this.debugFormattedLog("ChunkPath: {0}", this.client.debugChunkInfo ? "shown" : "hidden");
            return true;
         case 76:
            this.client.chunkCullingEnabled = !this.client.chunkCullingEnabled;
            this.debugFormattedLog("SmartCull: {0}", this.client.chunkCullingEnabled ? "enabled" : "disabled");
            return true;
         case 85:
            if (Screen.hasShiftDown()) {
               this.client.worldRenderer.killFrustum();
               this.debugFormattedLog("Killed frustum");
            } else {
               this.client.worldRenderer.captureFrustum();
               this.debugFormattedLog("Captured frustum");
            }

            return true;
         case 86:
            this.client.debugChunkOcclusion = !this.client.debugChunkOcclusion;
            this.debugFormattedLog("ChunkVisibility: {0}", this.client.debugChunkOcclusion ? "enabled" : "disabled");
            return true;
         case 87:
            this.client.wireFrame = !this.client.wireFrame;
            this.debugFormattedLog("WireFrame: {0}", this.client.wireFrame ? "enabled" : "disabled");
            return true;
         default:
            return false;
      }
   }

   private void addDebugMessage(Formatting formatting, Text text) {
      this.client.inGameHud.getChatHud().addMessage(Text.empty().append((Text)Text.translatable("debug.prefix").formatted(formatting, Formatting.BOLD)).append(ScreenTexts.SPACE).append(text));
   }

   private void debugLog(Text text) {
      this.addDebugMessage(Formatting.YELLOW, text);
   }

   private void debugLog(String key, Object... args) {
      this.debugLog(Text.translatable(key, args));
   }

   private void debugError(String key, Object... args) {
      this.addDebugMessage(Formatting.RED, Text.translatable(key, args));
   }

   private void debugFormattedLog(String pattern, Object... args) {
      this.debugLog(Text.literal(MessageFormat.format(pattern, args)));
   }

   private boolean processF3(int key) {
      if (this.debugCrashStartTime > 0L && this.debugCrashStartTime < Util.getMeasuringTimeMs() - 100L) {
         return true;
      } else {
         switch (key) {
            case 65:
               this.client.worldRenderer.reload();
               this.debugLog("debug.reload_chunks.message");
               return true;
            case 66:
               boolean bl = !this.client.getEntityRenderDispatcher().shouldRenderHitboxes();
               this.client.getEntityRenderDispatcher().setRenderHitboxes(bl);
               this.debugLog(bl ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
               return true;
            case 67:
               if (this.client.player.hasReducedDebugInfo()) {
                  return false;
               } else {
                  ClientPlayNetworkHandler lv3 = this.client.player.networkHandler;
                  if (lv3 == null) {
                     return false;
                  }

                  this.debugLog("debug.copy_location.message");
                  this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.client.player.getWorld().getRegistryKey().getValue(), this.client.player.getX(), this.client.player.getY(), this.client.player.getZ(), this.client.player.getYaw(), this.client.player.getPitch()));
                  return true;
               }
            case 68:
               if (this.client.inGameHud != null) {
                  this.client.inGameHud.getChatHud().clear(false);
               }

               return true;
            case 71:
               boolean bl2 = this.client.debugRenderer.toggleShowChunkBorder();
               this.debugLog(bl2 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
               return true;
            case 72:
               this.client.options.advancedItemTooltips = !this.client.options.advancedItemTooltips;
               this.debugLog(this.client.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
               this.client.options.write();
               return true;
            case 73:
               if (!this.client.player.hasReducedDebugInfo()) {
                  this.copyLookAt(this.client.player.hasPermissionLevel(2), !Screen.hasShiftDown());
               }

               return true;
            case 76:
               if (this.client.toggleDebugProfiler(this::debugLog)) {
                  this.debugLog("debug.profiling.start", 10);
               }

               return true;
            case 78:
               if (!this.client.player.hasPermissionLevel(2)) {
                  this.debugLog("debug.creative_spectator.error");
               } else if (!this.client.player.isSpectator()) {
                  this.client.player.networkHandler.sendCommand("gamemode spectator");
               } else {
                  ClientPlayNetworkHandler var10000 = this.client.player.networkHandler;
                  GameMode var10001 = this.client.interactionManager.getPreviousGameMode();
                  var10000.sendCommand("gamemode " + ((GameMode)MoreObjects.firstNonNull(var10001, GameMode.CREATIVE)).getName());
               }

               return true;
            case 80:
               this.client.options.pauseOnLostFocus = !this.client.options.pauseOnLostFocus;
               this.client.options.write();
               this.debugLog(this.client.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
               return true;
            case 81:
               this.debugLog("debug.help.message");
               ChatHud lv = this.client.inGameHud.getChatHud();
               lv.addMessage(Text.translatable("debug.reload_chunks.help"));
               lv.addMessage(Text.translatable("debug.show_hitboxes.help"));
               lv.addMessage(Text.translatable("debug.copy_location.help"));
               lv.addMessage(Text.translatable("debug.clear_chat.help"));
               lv.addMessage(Text.translatable("debug.chunk_boundaries.help"));
               lv.addMessage(Text.translatable("debug.advanced_tooltips.help"));
               lv.addMessage(Text.translatable("debug.inspect.help"));
               lv.addMessage(Text.translatable("debug.profiling.help"));
               lv.addMessage(Text.translatable("debug.creative_spectator.help"));
               lv.addMessage(Text.translatable("debug.pause_focus.help"));
               lv.addMessage(Text.translatable("debug.help.help"));
               lv.addMessage(Text.translatable("debug.dump_dynamic_textures.help"));
               lv.addMessage(Text.translatable("debug.reload_resourcepacks.help"));
               lv.addMessage(Text.translatable("debug.pause.help"));
               lv.addMessage(Text.translatable("debug.gamemodes.help"));
               return true;
            case 83:
               Path path = this.client.runDirectory.toPath().toAbsolutePath();
               Path path2 = TextureUtil.getDebugTexturePath(path);
               this.client.getTextureManager().dumpDynamicTextures(path2);
               Text lv2 = Text.literal(path.relativize(path2).toString()).formatted(Formatting.UNDERLINE).styled((style) -> {
                  return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path2.toFile().toString()));
               });
               this.debugLog("debug.dump_dynamic_textures", lv2);
               return true;
            case 84:
               this.debugLog("debug.reload_resourcepacks.message");
               this.client.reloadResources();
               return true;
            case 293:
               if (!this.client.player.hasPermissionLevel(2)) {
                  this.debugLog("debug.gamemodes.error");
               } else {
                  this.client.setScreen(new GameModeSelectionScreen());
               }

               return true;
            default:
               return false;
         }
      }
   }

   private void copyLookAt(boolean hasQueryPermission, boolean queryServer) {
      HitResult lv = this.client.crosshairTarget;
      if (lv != null) {
         switch (lv.getType()) {
            case BLOCK:
               BlockPos lv2 = ((BlockHitResult)lv).getBlockPos();
               BlockState lv3 = this.client.player.getWorld().getBlockState(lv2);
               if (hasQueryPermission) {
                  if (queryServer) {
                     this.client.player.networkHandler.getDataQueryHandler().queryBlockNbt(lv2, (nbt) -> {
                        this.copyBlock(lv3, lv2, nbt);
                        this.debugLog("debug.inspect.server.block");
                     });
                  } else {
                     BlockEntity lv4 = this.client.player.getWorld().getBlockEntity(lv2);
                     NbtCompound lv5 = lv4 != null ? lv4.createNbt() : null;
                     this.copyBlock(lv3, lv2, lv5);
                     this.debugLog("debug.inspect.client.block");
                  }
               } else {
                  this.copyBlock(lv3, lv2, (NbtCompound)null);
                  this.debugLog("debug.inspect.client.block");
               }
               break;
            case ENTITY:
               Entity lv6 = ((EntityHitResult)lv).getEntity();
               Identifier lv7 = Registries.ENTITY_TYPE.getId(lv6.getType());
               if (hasQueryPermission) {
                  if (queryServer) {
                     this.client.player.networkHandler.getDataQueryHandler().queryEntityNbt(lv6.getId(), (nbt) -> {
                        this.copyEntity(lv7, lv6.getPos(), nbt);
                        this.debugLog("debug.inspect.server.entity");
                     });
                  } else {
                     NbtCompound lv8 = lv6.writeNbt(new NbtCompound());
                     this.copyEntity(lv7, lv6.getPos(), lv8);
                     this.debugLog("debug.inspect.client.entity");
                  }
               } else {
                  this.copyEntity(lv7, lv6.getPos(), (NbtCompound)null);
                  this.debugLog("debug.inspect.client.entity");
               }
         }

      }
   }

   private void copyBlock(BlockState state, BlockPos pos, @Nullable NbtCompound nbt) {
      StringBuilder stringBuilder = new StringBuilder(BlockArgumentParser.stringifyBlockState(state));
      if (nbt != null) {
         stringBuilder.append(nbt);
      }

      String string = String.format(Locale.ROOT, "/setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), stringBuilder);
      this.setClipboard(string);
   }

   private void copyEntity(Identifier id, Vec3d pos, @Nullable NbtCompound nbt) {
      String string2;
      if (nbt != null) {
         nbt.remove("UUID");
         nbt.remove("Pos");
         nbt.remove("Dimension");
         String string = NbtHelper.toPrettyPrintedText(nbt).getString();
         string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", id.toString(), pos.x, pos.y, pos.z, string);
      } else {
         string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", id.toString(), pos.x, pos.y, pos.z);
      }

      this.setClipboard(string2);
   }

   public void onKey(long window, int key, int scancode, int action, int modifiers) {
      if (window == this.client.getWindow().getHandle()) {
         if (this.debugCrashStartTime > 0L) {
            if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_C) || !InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
               this.debugCrashStartTime = -1L;
            }
         } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_C) && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
            this.switchF3State = true;
            this.debugCrashStartTime = Util.getMeasuringTimeMs();
            this.debugCrashLastLogTime = Util.getMeasuringTimeMs();
            this.debugCrashElapsedTime = 0L;
         }

         Screen lv = this.client.currentScreen;
         if (lv != null) {
            switch (key) {
               case 258:
                  this.client.setNavigationType(GuiNavigationType.KEYBOARD_TAB);
               case 259:
               case 260:
               case 261:
               default:
                  break;
               case 262:
               case 263:
               case 264:
               case 265:
                  this.client.setNavigationType(GuiNavigationType.KEYBOARD_ARROW);
            }
         }

         if (action == 1 && (!(this.client.currentScreen instanceof KeybindsScreen) || ((KeybindsScreen)lv).lastKeyCodeUpdateTime <= Util.getMeasuringTimeMs() - 20L)) {
            if (this.client.options.fullscreenKey.matchesKey(key, scancode)) {
               this.client.getWindow().toggleFullscreen();
               this.client.options.getFullscreen().setValue(this.client.getWindow().isFullscreen());
               return;
            }

            if (this.client.options.screenshotKey.matchesKey(key, scancode)) {
               if (Screen.hasControlDown()) {
               }

               ScreenshotRecorder.saveScreenshot(this.client.runDirectory, this.client.getFramebuffer(), (message) -> {
                  this.client.execute(() -> {
                     this.client.inGameHud.getChatHud().addMessage(message);
                  });
               });
               return;
            }
         }

         boolean bl2;
         if (this.client.getNarratorManager().isActive()) {
            boolean bl = lv == null || !(lv.getFocused() instanceof TextFieldWidget) || !((TextFieldWidget)lv.getFocused()).isActive();
            if (action != 0 && key == GLFW.GLFW_KEY_B && Screen.hasControlDown() && bl) {
               bl2 = this.client.options.getNarrator().getValue() == NarratorMode.OFF;
               this.client.options.getNarrator().setValue(NarratorMode.byId(((NarratorMode)this.client.options.getNarrator().getValue()).getId() + 1));
               if (lv instanceof SimpleOptionsScreen) {
                  ((SimpleOptionsScreen)lv).updateNarratorButtonText();
               }

               if (bl2 && lv != null) {
                  lv.applyNarratorModeChangeDelay();
               }
            }
         }

         if (lv != null) {
            boolean[] bls = new boolean[]{false};
            Screen.wrapScreenError(() -> {
               if (action != 1 && action != 2) {
                  if (action == 0) {
                     bls[0] = lv.keyReleased(key, scancode, modifiers);
                  }
               } else {
                  lv.applyKeyPressNarratorDelay();
                  bls[0] = lv.keyPressed(key, scancode, modifiers);
               }

            }, "keyPressed event handler", lv.getClass().getCanonicalName());
            if (bls[0]) {
               return;
            }
         }

         if (this.client.currentScreen == null) {
            InputUtil.Key lv2 = InputUtil.fromKeyCode(key, scancode);
            if (action == 0) {
               KeyBinding.setKeyPressed(lv2, false);
               if (key == GLFW.GLFW_KEY_F3) {
                  if (this.switchF3State) {
                     this.switchF3State = false;
                  } else {
                     this.client.options.debugEnabled = !this.client.options.debugEnabled;
                     this.client.options.debugProfilerEnabled = this.client.options.debugEnabled && Screen.hasShiftDown();
                     this.client.options.debugTpsEnabled = this.client.options.debugEnabled && Screen.hasAltDown();
                  }
               }
            } else {
               if (key == GLFW.GLFW_KEY_F4 && this.client.gameRenderer != null) {
                  this.client.gameRenderer.togglePostProcessorEnabled();
               }

               bl2 = false;
               if (key == GLFW.GLFW_KEY_ESCAPE) {
                  boolean bl3 = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3);
                  this.client.openPauseMenu(bl3);
               }

               bl2 = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3) && this.processF3(key);
               this.switchF3State |= bl2;
               if (key == GLFW.GLFW_KEY_F1) {
                  this.client.options.hudHidden = !this.client.options.hudHidden;
               }

               if (bl2) {
                  KeyBinding.setKeyPressed(lv2, false);
               } else {
                  KeyBinding.setKeyPressed(lv2, true);
                  KeyBinding.onKeyPressed(lv2);
               }

               if (this.client.options.debugProfilerEnabled && key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
                  this.client.handleProfilerKeyPress(key - GLFW.GLFW_KEY_0);
               }
            }
         }

      }
   }

   private void onChar(long window, int codePoint, int modifiers) {
      if (window == this.client.getWindow().getHandle()) {
         Element lv = this.client.currentScreen;
         if (lv != null && this.client.getOverlay() == null) {
            if (Character.charCount(codePoint) == 1) {
               Screen.wrapScreenError(() -> {
                  lv.charTyped((char)codePoint, modifiers);
               }, "charTyped event handler", lv.getClass().getCanonicalName());
            } else {
               char[] var6 = Character.toChars(codePoint);
               int var7 = var6.length;

               for(int var8 = 0; var8 < var7; ++var8) {
                  char c = var6[var8];
                  Screen.wrapScreenError(() -> {
                     lv.charTyped(c, modifiers);
                  }, "charTyped event handler", lv.getClass().getCanonicalName());
               }
            }

         }
      }
   }

   public void setup(long window) {
      InputUtil.setKeyboardCallbacks(window, (windowx, key, scancode, action, modifiers) -> {
         this.client.execute(() -> {
            this.onKey(windowx, key, scancode, action, modifiers);
         });
      }, (windowx, codePoint, modifiers) -> {
         this.client.execute(() -> {
            this.onChar(windowx, codePoint, modifiers);
         });
      });
   }

   public String getClipboard() {
      return this.clipboard.getClipboard(this.client.getWindow().getHandle(), (error, description) -> {
         if (error != 65545) {
            this.client.getWindow().logGlError(error, description);
         }

      });
   }

   public void setClipboard(String clipboard) {
      if (!clipboard.isEmpty()) {
         this.clipboard.setClipboard(this.client.getWindow().getHandle(), clipboard);
      }

   }

   public void pollDebugCrash() {
      if (this.debugCrashStartTime > 0L) {
         long l = Util.getMeasuringTimeMs();
         long m = 10000L - (l - this.debugCrashStartTime);
         long n = l - this.debugCrashLastLogTime;
         if (m < 0L) {
            if (Screen.hasControlDown()) {
               GlfwUtil.makeJvmCrash();
            }

            String string = "Manually triggered debug crash";
            CrashReport lv = new CrashReport("Manually triggered debug crash", new Throwable("Manually triggered debug crash"));
            CrashReportSection lv2 = lv.addElement("Manual crash details");
            WinNativeModuleUtil.addDetailTo(lv2);
            throw new CrashException(lv);
         }

         if (n >= 1000L) {
            if (this.debugCrashElapsedTime == 0L) {
               this.debugLog("debug.crash.message");
            } else {
               this.debugError("debug.crash.warning", MathHelper.ceil((float)m / 1000.0F));
            }

            this.debugCrashLastLogTime = l;
            ++this.debugCrashElapsedTime;
         }
      }

   }
}
