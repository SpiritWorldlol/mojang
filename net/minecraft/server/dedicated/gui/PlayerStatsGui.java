package net.minecraft.server.dedicated.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.Timer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;

public class PlayerStatsGui extends JComponent {
   private static final DecimalFormat AVG_TICK_FORMAT = (DecimalFormat)Util.make(new DecimalFormat("########0.000"), (decimalFormat) -> {
      decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
   });
   private final int[] memoryUsePercentage = new int[256];
   private int memoryUsePercentagePos;
   private final String[] lines = new String[11];
   private final MinecraftServer server;
   private final Timer timer;

   public PlayerStatsGui(MinecraftServer server) {
      this.server = server;
      this.setPreferredSize(new Dimension(456, 246));
      this.setMinimumSize(new Dimension(456, 246));
      this.setMaximumSize(new Dimension(456, 246));
      this.timer = new Timer(500, (event) -> {
         this.update();
      });
      this.timer.start();
      this.setBackground(Color.BLACK);
   }

   private void update() {
      long l = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
      this.lines[0] = "Memory use: " + l / 1024L / 1024L + " mb (" + Runtime.getRuntime().freeMemory() * 100L / Runtime.getRuntime().maxMemory() + "% free)";
      String[] var10000 = this.lines;
      DecimalFormat var10002 = AVG_TICK_FORMAT;
      double var10003 = this.average(this.server.lastTickLengths);
      var10000[1] = "Avg tick: " + var10002.format(var10003 * 1.0E-6) + " ms";
      this.memoryUsePercentage[this.memoryUsePercentagePos++ & 255] = (int)(l * 100L / Runtime.getRuntime().maxMemory());
      this.repaint();
   }

   private double average(long[] values) {
      long l = 0L;
      long[] var4 = values;
      int var5 = values.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         long m = var4[var6];
         l += m;
      }

      return (double)l / (double)values.length;
   }

   public void paint(Graphics graphics) {
      graphics.setColor(new Color(16777215));
      graphics.fillRect(0, 0, 456, 246);

      int i;
      for(i = 0; i < 256; ++i) {
         int j = this.memoryUsePercentage[i + this.memoryUsePercentagePos & 255];
         graphics.setColor(new Color(j + 28 << 16));
         graphics.fillRect(i, 100 - j, 1, j);
      }

      graphics.setColor(Color.BLACK);

      for(i = 0; i < this.lines.length; ++i) {
         String string = this.lines[i];
         if (string != null) {
            graphics.drawString(string, 32, 116 + i * 16);
         }
      }

   }

   public void stop() {
      this.timer.stop();
   }
}
