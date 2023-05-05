package net.minecraft.client.gui.hud.spectator;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public class TeleportSpectatorMenu implements SpectatorMenuCommandGroup, SpectatorMenuCommand {
   private static final Comparator ORDERING = Comparator.comparing((a) -> {
      return a.getProfile().getId();
   });
   private static final Text TELEPORT_TEXT = Text.translatable("spectatorMenu.teleport");
   private static final Text PROMPT_TEXT = Text.translatable("spectatorMenu.teleport.prompt");
   private final List elements;

   public TeleportSpectatorMenu() {
      this(MinecraftClient.getInstance().getNetworkHandler().getListedPlayerListEntries());
   }

   public TeleportSpectatorMenu(Collection entries) {
      this.elements = entries.stream().filter((entry) -> {
         return entry.getGameMode() != GameMode.SPECTATOR;
      }).sorted(ORDERING).map((entry) -> {
         return new TeleportToSpecificPlayerSpectatorCommand(entry.getProfile());
      }).toList();
   }

   public List getCommands() {
      return this.elements;
   }

   public Text getPrompt() {
      return PROMPT_TEXT;
   }

   public void use(SpectatorMenu menu) {
      menu.selectElement(this);
   }

   public Text getName() {
      return TELEPORT_TEXT;
   }

   public void renderIcon(DrawContext context, float brightness, int alpha) {
      context.drawTexture(SpectatorHud.SPECTATOR_TEXTURE, 0, 0, 0.0F, 0.0F, 16, 16, 256, 256);
   }

   public boolean isEnabled() {
      return !this.elements.isEmpty();
   }
}
