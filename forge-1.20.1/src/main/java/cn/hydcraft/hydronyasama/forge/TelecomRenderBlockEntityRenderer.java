package cn.hydcraft.hydronyasama.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

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
    if (entity.getLevel() == null) {
      return;
    }

    ItemStack stack = new ItemStack(entity.getBlockState().getBlock().asItem());
    if (stack.isEmpty()) {
      return;
    }

    var itemRenderer = Minecraft.getInstance().getItemRenderer();

    poseStack.pushPose();
    poseStack.translate(0.5D, 0.30D, 0.5D);
    poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
    poseStack.scale(0.90F, 0.90F, 0.90F);
    itemRenderer.renderStatic(
        stack,
        ItemDisplayContext.FIXED,
        packedLight,
        packedOverlay,
        poseStack,
        buffer,
        entity.getLevel(),
        0);
    poseStack.popPose();

    poseStack.pushPose();
    poseStack.translate(0.5D, 0.30D, 0.5D);
    poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
    poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
    poseStack.scale(0.90F, 0.90F, 0.90F);
    itemRenderer.renderStatic(
        stack,
        ItemDisplayContext.FIXED,
        packedLight,
        packedOverlay,
        poseStack,
        buffer,
        entity.getLevel(),
        0);
    poseStack.popPose();
  }
}
