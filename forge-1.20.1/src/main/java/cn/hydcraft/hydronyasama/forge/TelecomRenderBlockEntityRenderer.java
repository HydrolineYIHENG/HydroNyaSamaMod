package cn.hydcraft.hydronyasama.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

final class TelecomRenderBlockEntityRenderer implements BlockEntityRenderer<TelecomRenderBlockEntity> {
  TelecomRenderBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(
      TelecomRenderBlockEntity entity,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      int packedOverlay) {
    // Disabled temporary overlay renderer; telecom block visuals are driven by block models now.
  }
}
