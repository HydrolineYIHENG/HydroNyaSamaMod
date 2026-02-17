package cn.hydcraft.hydronyasama.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

public final class OpticsTextBlockEntityRenderer
    extends BlockEntityRenderer<OpticsTextBlockEntity> {
  public OpticsTextBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  public void render(
      OpticsTextBlockEntity entity,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      int packedOverlay) {
    String text = entity.text();
    if (text.isEmpty()) {
      return;
    }
    Minecraft mc = Minecraft.getInstance();
    Font font = mc.font;
    poseStack.pushPose();
    poseStack.translate(0.5D, 0.72D, 0.5D);
    poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
    poseStack.scale(-0.02F, -0.02F, 0.02F);
    float x = -font.width(text) / 2.0F;
    font.draw(poseStack, text, x, 0.0F, 0xFFFFFFFF);
    poseStack.popPose();
  }
}
