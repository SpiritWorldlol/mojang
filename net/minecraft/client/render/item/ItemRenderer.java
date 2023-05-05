package net.minecraft.client.render.item;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MatrixUtil;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ItemRenderer implements SynchronousResourceReloader {
   public static final Identifier ENTITY_ENCHANTMENT_GLINT = new Identifier("textures/misc/enchanted_glint_entity.png");
   public static final Identifier ITEM_ENCHANTMENT_GLINT = new Identifier("textures/misc/enchanted_glint_item.png");
   private static final Set WITHOUT_MODELS;
   public static final int field_32937 = 8;
   public static final int field_32938 = 8;
   public static final int field_32934 = 200;
   public static final float COMPASS_WITH_GLINT_GUI_MODEL_MULTIPLIER = 0.5F;
   public static final float COMPASS_WITH_GLINT_FIRST_PERSON_MODEL_MULTIPLIER = 0.75F;
   public static final float field_41120 = 0.0078125F;
   private static final ModelIdentifier TRIDENT;
   public static final ModelIdentifier TRIDENT_IN_HAND;
   private static final ModelIdentifier SPYGLASS;
   public static final ModelIdentifier SPYGLASS_IN_HAND;
   private final MinecraftClient client;
   private final ItemModels models;
   private final TextureManager textureManager;
   private final ItemColors colors;
   private final BuiltinModelItemRenderer builtinModelItemRenderer;

   public ItemRenderer(MinecraftClient client, TextureManager manager, BakedModelManager bakery, ItemColors colors, BuiltinModelItemRenderer builtinModelItemRenderer) {
      this.client = client;
      this.textureManager = manager;
      this.models = new ItemModels(bakery);
      this.builtinModelItemRenderer = builtinModelItemRenderer;
      Iterator var6 = Registries.ITEM.iterator();

      while(var6.hasNext()) {
         Item lv = (Item)var6.next();
         if (!WITHOUT_MODELS.contains(lv)) {
            this.models.putModel(lv, new ModelIdentifier(Registries.ITEM.getId(lv), "inventory"));
         }
      }

      this.colors = colors;
   }

   public ItemModels getModels() {
      return this.models;
   }

   private void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {
      Random lv = Random.create();
      long l = 42L;
      Direction[] var10 = Direction.values();
      int var11 = var10.length;

      for(int var12 = 0; var12 < var11; ++var12) {
         Direction lv2 = var10[var12];
         lv.setSeed(42L);
         this.renderBakedItemQuads(matrices, vertices, model.getQuads((BlockState)null, lv2, lv), stack, light, overlay);
      }

      lv.setSeed(42L);
      this.renderBakedItemQuads(matrices, vertices, model.getQuads((BlockState)null, (Direction)null, lv), stack, light, overlay);
   }

   public void renderItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
      if (!stack.isEmpty()) {
         matrices.push();
         boolean bl2 = renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED;
         if (bl2) {
            if (stack.isOf(Items.TRIDENT)) {
               model = this.models.getModelManager().getModel(TRIDENT);
            } else if (stack.isOf(Items.SPYGLASS)) {
               model = this.models.getModelManager().getModel(SPYGLASS);
            }
         }

         model.getTransformation().getTransformation(renderMode).apply(leftHanded, matrices);
         matrices.translate(-0.5F, -0.5F, -0.5F);
         if (!model.isBuiltin() && (!stack.isOf(Items.TRIDENT) || bl2)) {
            boolean bl3;
            if (renderMode != ModelTransformationMode.GUI && !renderMode.isFirstPerson() && stack.getItem() instanceof BlockItem) {
               Block lv = ((BlockItem)stack.getItem()).getBlock();
               bl3 = !(lv instanceof TransparentBlock) && !(lv instanceof StainedGlassPaneBlock);
            } else {
               bl3 = true;
            }

            RenderLayer lv2 = RenderLayers.getItemLayer(stack, bl3);
            VertexConsumer lv4;
            if (stack.isIn(ItemTags.COMPASSES) && stack.hasGlint()) {
               matrices.push();
               MatrixStack.Entry lv3 = matrices.peek();
               if (renderMode == ModelTransformationMode.GUI) {
                  MatrixUtil.scale(lv3.getPositionMatrix(), 0.5F);
               } else if (renderMode.isFirstPerson()) {
                  MatrixUtil.scale(lv3.getPositionMatrix(), 0.75F);
               }

               if (bl3) {
                  lv4 = getDirectCompassGlintConsumer(vertexConsumers, lv2, lv3);
               } else {
                  lv4 = getCompassGlintConsumer(vertexConsumers, lv2, lv3);
               }

               matrices.pop();
            } else if (bl3) {
               lv4 = getDirectItemGlintConsumer(vertexConsumers, lv2, true, stack.hasGlint());
            } else {
               lv4 = getItemGlintConsumer(vertexConsumers, lv2, true, stack.hasGlint());
            }

            this.renderBakedItemModel(model, stack, light, overlay, matrices, lv4);
         } else {
            this.builtinModelItemRenderer.render(stack, renderMode, matrices, vertexConsumers, light, overlay);
         }

         matrices.pop();
      }
   }

   public static VertexConsumer getArmorGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, boolean solid, boolean glint) {
      return glint ? VertexConsumers.union(provider.getBuffer(solid ? RenderLayer.getArmorGlint() : RenderLayer.getArmorEntityGlint()), provider.getBuffer(layer)) : provider.getBuffer(layer);
   }

   public static VertexConsumer getCompassGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, MatrixStack.Entry entry) {
      return VertexConsumers.union(new OverlayVertexConsumer(provider.getBuffer(RenderLayer.getGlint()), entry.getPositionMatrix(), entry.getNormalMatrix(), 0.0078125F), provider.getBuffer(layer));
   }

   public static VertexConsumer getDirectCompassGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, MatrixStack.Entry entry) {
      return VertexConsumers.union(new OverlayVertexConsumer(provider.getBuffer(RenderLayer.getDirectGlint()), entry.getPositionMatrix(), entry.getNormalMatrix(), 0.0078125F), provider.getBuffer(layer));
   }

   public static VertexConsumer getItemGlintConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint) {
      if (glint) {
         return MinecraftClient.isFabulousGraphicsOrBetter() && layer == TexturedRenderLayers.getItemEntityTranslucentCull() ? VertexConsumers.union(vertexConsumers.getBuffer(RenderLayer.getGlintTranslucent()), vertexConsumers.getBuffer(layer)) : VertexConsumers.union(vertexConsumers.getBuffer(solid ? RenderLayer.getGlint() : RenderLayer.getEntityGlint()), vertexConsumers.getBuffer(layer));
      } else {
         return vertexConsumers.getBuffer(layer);
      }
   }

   public static VertexConsumer getDirectItemGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, boolean solid, boolean glint) {
      return glint ? VertexConsumers.union(provider.getBuffer(solid ? RenderLayer.getDirectGlint() : RenderLayer.getDirectEntityGlint()), provider.getBuffer(layer)) : provider.getBuffer(layer);
   }

   private void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List quads, ItemStack stack, int light, int overlay) {
      boolean bl = !stack.isEmpty();
      MatrixStack.Entry lv = matrices.peek();
      Iterator var9 = quads.iterator();

      while(var9.hasNext()) {
         BakedQuad lv2 = (BakedQuad)var9.next();
         int k = -1;
         if (bl && lv2.hasColor()) {
            k = this.colors.getColor(stack, lv2.getColorIndex());
         }

         float f = (float)(k >> 16 & 255) / 255.0F;
         float g = (float)(k >> 8 & 255) / 255.0F;
         float h = (float)(k & 255) / 255.0F;
         vertices.quad(lv, lv2, f, g, h, light, overlay);
      }

   }

   public BakedModel getModel(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed) {
      BakedModel lv;
      if (stack.isOf(Items.TRIDENT)) {
         lv = this.models.getModelManager().getModel(TRIDENT_IN_HAND);
      } else if (stack.isOf(Items.SPYGLASS)) {
         lv = this.models.getModelManager().getModel(SPYGLASS_IN_HAND);
      } else {
         lv = this.models.getModel(stack);
      }

      ClientWorld lv2 = world instanceof ClientWorld ? (ClientWorld)world : null;
      BakedModel lv3 = lv.getOverrides().apply(lv, stack, lv2, entity, seed);
      return lv3 == null ? this.models.getModelManager().getMissingModel() : lv3;
   }

   public void renderItem(ItemStack stack, ModelTransformationMode transformationType, int light, int overlay, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int seed) {
      this.renderItem((LivingEntity)null, stack, transformationType, false, matrices, vertexConsumers, world, light, overlay, seed);
   }

   public void renderItem(@Nullable LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int light, int overlay, int seed) {
      if (!item.isEmpty()) {
         BakedModel lv = this.getModel(item, world, entity, seed);
         this.renderItem(item, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, lv);
      }
   }

   public void reload(ResourceManager manager) {
      this.models.reloadModels();
   }

   static {
      WITHOUT_MODELS = Sets.newHashSet(new Item[]{Items.AIR});
      TRIDENT = ModelIdentifier.ofVanilla("trident", "inventory");
      TRIDENT_IN_HAND = ModelIdentifier.ofVanilla("trident_in_hand", "inventory");
      SPYGLASS = ModelIdentifier.ofVanilla("spyglass", "inventory");
      SPYGLASS_IN_HAND = ModelIdentifier.ofVanilla("spyglass_in_hand", "inventory");
   }
}
