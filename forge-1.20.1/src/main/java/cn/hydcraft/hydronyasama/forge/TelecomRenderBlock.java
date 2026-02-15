package cn.hydcraft.hydronyasama.forge;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;

final class TelecomRenderBlock extends BaseEntityBlock {
  TelecomRenderBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    // Keep the base cube model visible and only overlay telemetry panel in BER.
    return RenderShape.MODEL;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TelecomRenderBlockEntity(pos, state);
  }
}
