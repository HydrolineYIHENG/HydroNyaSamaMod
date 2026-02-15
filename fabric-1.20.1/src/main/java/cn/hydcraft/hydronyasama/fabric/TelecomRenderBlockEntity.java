package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.fabric.content.FabricContentRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TelecomRenderBlockEntity extends BlockEntity {
  public TelecomRenderBlockEntity(BlockPos pos, BlockState state) {
    super(FabricContentRegistrar.telecomRenderBlockEntityType(), pos, state);
  }
}
