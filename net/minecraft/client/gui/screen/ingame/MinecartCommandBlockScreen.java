package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.world.CommandBlockExecutor;

@Environment(EnvType.CLIENT)
public class MinecartCommandBlockScreen extends AbstractCommandBlockScreen {
   private final CommandBlockExecutor commandExecutor;

   public MinecartCommandBlockScreen(CommandBlockExecutor commandExecutor) {
      this.commandExecutor = commandExecutor;
   }

   public CommandBlockExecutor getCommandExecutor() {
      return this.commandExecutor;
   }

   int getTrackOutputButtonHeight() {
      return 150;
   }

   protected void init() {
      super.init();
      this.consoleCommandTextField.setText(this.getCommandExecutor().getCommand());
   }

   protected void syncSettingsToServer(CommandBlockExecutor commandExecutor) {
      if (commandExecutor instanceof CommandBlockMinecartEntity.CommandExecutor lv) {
         this.client.getNetworkHandler().sendPacket(new UpdateCommandBlockMinecartC2SPacket(lv.getMinecart().getId(), this.consoleCommandTextField.getText(), commandExecutor.isTrackingOutput()));
      }

   }
}
