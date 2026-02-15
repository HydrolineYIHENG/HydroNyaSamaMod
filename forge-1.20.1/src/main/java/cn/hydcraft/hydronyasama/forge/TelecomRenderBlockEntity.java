package cn.hydcraft.hydronyasama.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

final class TelecomRenderBlockEntity extends BlockEntity {
  TelecomRenderBlockEntity(BlockPos pos, BlockState state) {
    super(ForgeContentRegistry.telecomRenderBlockEntityType(), pos, state);
  }
}
