package cn.hydcraft.hydronyasama.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

final class TelecomRenderBlockEntityRenderer
    implements BlockEntityRenderer<TelecomRenderBlockEntity> {
  private static final ResourceLocation TEX_RED =
      new ResourceLocation("hydronyasama", "textures/block/signal_box_r.png");
  private static final ResourceLocation TEX_YELLOW =
      new ResourceLocation("hydronyasama", "textures/block/signal_box_y.png");
  private static final ResourceLocation TEX_GREEN =
      new ResourceLocation("hydronyasama", "textures/block/signal_box_g.png");
  private static final ResourceLocation TEX_BLUE =
      new ResourceLocation("hydronyasama", "textures/block/signal_box_b.png");
  private static final ResourceLocation TEX_WHITE =
      new ResourceLocation("hydronyasama", "textures/block/signal_box_w.png");
  private static final ResourceLocation TEX_OFF =
      new ResourceLocation("hydronyasama", "textures/block/signal_box_none.png");

  TelecomRenderBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(
      TelecomRenderBlockEntity entity,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      int packedOverlay) {
    BlockState state = entity.getBlockState();
    String blockPath = TelecomToolSupport.resolveTelecomBlockPath(state);
    if (blockPath.isEmpty()) {
      return;
    }
    if (!showLampOverlay(blockPath)) {
      return;
    }

    boolean isThin = blockPath.startsWith("nspga_");
    float y = (isThin ? 2.0F : 8.0F) / 16.0F + 0.002F;

    boolean input = entity.telecomInput();
    boolean enabled = entity.telecomEnabled();
    boolean output = entity.telecomOutput();

    // Match legacy sign positions from signal_box_sign*.obj
    drawLamp(
        poseStack,
        buffer,
        5.0F / 16.0F,
        6.0F / 16.0F,
        y,
        10.0F / 16.0F,
        11.0F / 16.0F,
        enabled ? TEX_RED : TEX_OFF,
        true,
        packedLight,
        packedOverlay);
    drawLamp(
        poseStack,
        buffer,
        7.0F / 16.0F,
        8.0F / 16.0F,
        y,
        11.0F / 16.0F,
        10.0F / 16.0F,
        input ? TEX_YELLOW : TEX_OFF,
        true,
        packedLight,
        packedOverlay);
    drawLamp(
        poseStack,
        buffer,
        5.0F / 16.0F,
        6.0F / 16.0F,
        y,
        8.0F / 16.0F,
        9.0F / 16.0F,
        output ? TEX_GREEN : TEX_OFF,
        true,
        packedLight,
        packedOverlay);

    if ("tri_state_signal_box".equals(blockPath)) {
      drawLamp(
          poseStack,
          buffer,
          5.0F / 16.0F,
          6.0F / 16.0F,
          y,
          7.0F / 16.0F,
          8.0F / 16.0F,
          output ? TEX_WHITE : TEX_BLUE,
          true,
          packedLight,
          packedOverlay);
    }

    if (hasButtonLamp(blockPath)) {
      drawLamp(
          poseStack,
          buffer,
          6.0F / 16.0F,
          10.0F / 16.0F,
          y,
          5.0F / 16.0F,
          7.0F / 16.0F,
          enabled ? TEX_WHITE : TEX_OFF,
          true,
          packedLight,
          packedOverlay);
    }
  }

  private static boolean showLampOverlay(String blockPath) {
    if ("nsasm_box".equals(blockPath) || blockPath.startsWith("nspga_")) {
      return false;
    }
    return true;
  }

  private static boolean hasButtonLamp(String blockPath) {
    return "signal_box_sender".equals(blockPath)
        || "signal_box_input".equals(blockPath)
        || "signal_box_rx".equals(blockPath)
        || "rs_latch".equals(blockPath)
        || "timer".equals(blockPath);
  }

  private static void drawLamp(
      PoseStack poseStack,
      MultiBufferSource buffer,
      float x0,
      float x1,
      float y,
      float z0,
      float z1,
      ResourceLocation texture,
      boolean emissive,
      int packedLight,
      int packedOverlay) {
    PoseStack.Pose pose = poseStack.last();
    Matrix4f matrix4f = pose.pose();
    Matrix3f matrix3f = pose.normal();
    VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
    int light = emissive ? LightTexture.FULL_BRIGHT : packedLight;

    addVertex(consumer, matrix4f, matrix3f, x0, y, z0, 0.0F, 0.0F, light, packedOverlay);
    addVertex(consumer, matrix4f, matrix3f, x1, y, z0, 1.0F, 0.0F, light, packedOverlay);
    addVertex(consumer, matrix4f, matrix3f, x1, y, z1, 1.0F, 1.0F, light, packedOverlay);
    addVertex(consumer, matrix4f, matrix3f, x0, y, z1, 0.0F, 1.0F, light, packedOverlay);
  }

  private static void addVertex(
      VertexConsumer consumer,
      Matrix4f pose,
      Matrix3f normal,
      float x,
      float y,
      float z,
      float u,
      float v,
      int light,
      int packedOverlay) {
    consumer
        .vertex(pose, x, y, z)
        .color(255, 255, 255, 255)
        .uv(u, v)
        .overlayCoords(packedOverlay)
        .uv2(light)
        .normal(normal, 0.0F, 1.0F, 0.0F)
        .endVertex();
  }
}
