package cn.hydcraft.hydronyasama.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class TelecomRenderBlockEntityRenderer
    implements BlockEntityRenderer<TelecomRenderBlockEntity> {
  public TelecomRenderBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

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
