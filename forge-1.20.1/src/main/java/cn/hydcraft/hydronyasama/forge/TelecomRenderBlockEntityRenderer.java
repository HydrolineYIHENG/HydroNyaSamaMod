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

    boolean isThin = blockPath.startsWith("nspga_");
    float y = (isThin ? 2.0F : 8.0F) / 16.0F + 0.002F;

    boolean input = entity.telecomInput();
    boolean enabled = entity.telecomEnabled();
    boolean output = entity.telecomOutput();

    // left lamp: input bus, middle lamp: core state, right lamp: output bus
    drawLamp(
        poseStack,
        buffer,
        4.0F / 16.0F,
        y,
        11.0F / 16.0F,
        2.0F / 16.0F,
        input ? TEX_YELLOW : TEX_OFF,
        input,
        packedLight,
        packedOverlay);
    drawLamp(
        poseStack,
        buffer,
        7.0F / 16.0F,
        y,
        11.0F / 16.0F,
        2.0F / 16.0F,
        enabled ? TEX_GREEN : TEX_RED,
        true,
        packedLight,
        packedOverlay);
    drawLamp(
        poseStack,
        buffer,
        10.0F / 16.0F,
        y,
        11.0F / 16.0F,
        2.0F / 16.0F,
        output ? TEX_BLUE : TEX_OFF,
        output,
        packedLight,
        packedOverlay);

    if (hasWhiteButton(blockPath)) {
      drawLamp(
          poseStack,
          buffer,
          7.0F / 16.0F,
          y,
          3.0F / 16.0F,
          2.0F / 16.0F,
          enabled ? TEX_GREEN : TEX_WHITE,
          enabled,
          packedLight,
          packedOverlay);
    }
  }

  private static boolean hasWhiteButton(String blockPath) {
    return "signal_box".equals(blockPath)
        || "signal_box_sender".equals(blockPath)
        || "signal_box_getter".equals(blockPath)
        || "tri_state_signal_box".equals(blockPath)
        || "signal_box_rx".equals(blockPath)
        || "signal_box_tx".equals(blockPath)
        || "signal_box_input".equals(blockPath)
        || "signal_box_output".equals(blockPath);
  }

  private static void drawLamp(
      PoseStack poseStack,
      MultiBufferSource buffer,
      float x,
      float y,
      float z,
      float size,
      ResourceLocation texture,
      boolean emissive,
      int packedLight,
      int packedOverlay) {
    PoseStack.Pose pose = poseStack.last();
    Matrix4f matrix4f = pose.pose();
    Matrix3f matrix3f = pose.normal();
    VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
    int light = emissive ? LightTexture.FULL_BRIGHT : packedLight;

    addVertex(consumer, matrix4f, matrix3f, x, y, z, 0.0F, 0.0F, light, packedOverlay);
    addVertex(consumer, matrix4f, matrix3f, x + size, y, z, 1.0F, 0.0F, light, packedOverlay);
    addVertex(
        consumer, matrix4f, matrix3f, x + size, y, z + size, 1.0F, 1.0F, light, packedOverlay);
    addVertex(consumer, matrix4f, matrix3f, x, y, z + size, 0.0F, 1.0F, light, packedOverlay);
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
