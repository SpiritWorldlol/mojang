package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.util.annotation.DeobfuscateClass;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

@Environment(EnvType.CLIENT)
@DeobfuscateClass
public class GLX {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static String cpuInfo;

   public static String getOpenGLVersionString() {
      RenderSystem.assertOnRenderThread();
      if (GLFW.glfwGetCurrentContext() == 0L) {
         return "NO CONTEXT";
      } else {
         String var10000 = GlStateManager._getString(GL11.GL_RENDERER);
         return var10000 + " GL version " + GlStateManager._getString(GL11.GL_VERSION) + ", " + GlStateManager._getString(GL11.GL_VENDOR);
      }
   }

   public static int _getRefreshRate(Window window) {
      RenderSystem.assertOnRenderThread();
      long l = GLFW.glfwGetWindowMonitor(window.getHandle());
      if (l == 0L) {
         l = GLFW.glfwGetPrimaryMonitor();
      }

      GLFWVidMode gLFWVidMode = l == 0L ? null : GLFW.glfwGetVideoMode(l);
      return gLFWVidMode == null ? 0 : gLFWVidMode.refreshRate();
   }

   public static String _getLWJGLVersion() {
      RenderSystem.assertInInitPhase();
      return Version.getVersion();
   }

   public static LongSupplier _initGlfw() {
      RenderSystem.assertInInitPhase();
      Window.acceptError((code, message) -> {
         throw new IllegalStateException(String.format(Locale.ROOT, "GLFW error before init: [0x%X]%s", code, message));
      });
      List list = Lists.newArrayList();
      GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback((code, pointer) -> {
         list.add(String.format(Locale.ROOT, "GLFW error during init: [0x%X]%s", code, pointer));
      });
      if (!GLFW.glfwInit()) {
         throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(list));
      } else {
         LongSupplier longSupplier = () -> {
            return (long)(GLFW.glfwGetTime() * 1.0E9);
         };
         Iterator var3 = list.iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();
            LOGGER.error("GLFW error collected during initialization: {}", string);
         }

         RenderSystem.setErrorCallback(gLFWErrorCallback);
         return longSupplier;
      }
   }

   public static void _setGlfwErrorCallback(GLFWErrorCallbackI callback) {
      RenderSystem.assertInInitPhase();
      GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(callback);
      if (gLFWErrorCallback != null) {
         gLFWErrorCallback.free();
      }

   }

   public static boolean _shouldClose(Window window) {
      return GLFW.glfwWindowShouldClose(window.getHandle());
   }

   public static void _init(int debugVerbosity, boolean debugSync) {
      RenderSystem.assertInInitPhase();

      try {
         CentralProcessor centralProcessor = (new SystemInfo()).getHardware().getProcessor();
         cpuInfo = String.format(Locale.ROOT, "%dx %s", centralProcessor.getLogicalProcessorCount(), centralProcessor.getProcessorIdentifier().getName()).replaceAll("\\s+", " ");
      } catch (Throwable var3) {
      }

      GlDebug.enableDebug(debugVerbosity, debugSync);
   }

   public static String _getCpuInfo() {
      return cpuInfo == null ? "<unknown>" : cpuInfo;
   }

   public static void _renderCrosshair(int size, boolean drawX, boolean drawY, boolean drawZ) {
      RenderSystem.assertOnRenderThread();
      GlStateManager._depthMask(false);
      GlStateManager._disableCull();
      RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
      Tessellator lv = RenderSystem.renderThreadTesselator();
      BufferBuilder lv2 = lv.getBuffer();
      RenderSystem.lineWidth(4.0F);
      lv2.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
      if (drawX) {
         lv2.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).next();
         lv2.vertex((double)size, 0.0, 0.0).color(0, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).next();
      }

      if (drawY) {
         lv2.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(0.0F, 1.0F, 0.0F).next();
         lv2.vertex(0.0, (double)size, 0.0).color(0, 0, 0, 255).normal(0.0F, 1.0F, 0.0F).next();
      }

      if (drawZ) {
         lv2.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(0.0F, 0.0F, 1.0F).next();
         lv2.vertex(0.0, 0.0, (double)size).color(0, 0, 0, 255).normal(0.0F, 0.0F, 1.0F).next();
      }

      lv.draw();
      RenderSystem.lineWidth(2.0F);
      lv2.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
      if (drawX) {
         lv2.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).next();
         lv2.vertex((double)size, 0.0, 0.0).color(255, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).next();
      }

      if (drawY) {
         lv2.vertex(0.0, 0.0, 0.0).color(0, 255, 0, 255).normal(0.0F, 1.0F, 0.0F).next();
         lv2.vertex(0.0, (double)size, 0.0).color(0, 255, 0, 255).normal(0.0F, 1.0F, 0.0F).next();
      }

      if (drawZ) {
         lv2.vertex(0.0, 0.0, 0.0).color(127, 127, 255, 255).normal(0.0F, 0.0F, 1.0F).next();
         lv2.vertex(0.0, 0.0, (double)size).color(127, 127, 255, 255).normal(0.0F, 0.0F, 1.0F).next();
      }

      lv.draw();
      RenderSystem.lineWidth(1.0F);
      GlStateManager._enableCull();
      GlStateManager._depthMask(true);
   }

   public static Object make(Supplier factory) {
      return factory.get();
   }

   public static Object make(Object object, Consumer initializer) {
      initializer.accept(object);
      return object;
   }
}
