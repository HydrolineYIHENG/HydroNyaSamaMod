package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TelecomInteractiveBlock extends Block {
  private static final VoxelShape THIN_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);
  private static final VoxelShape THICK_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);

  public TelecomInteractiveBlock(Properties properties) {
    super(properties);
  }

  @Override
  public VoxelShape getShape(
      BlockState state,
      net.minecraft.world.level.BlockGetter level,
      BlockPos pos,
      CollisionContext context) {
    return shapeForState(state);
  }

  @Override
  public VoxelShape getCollisionShape(
      BlockState state,
      net.minecraft.world.level.BlockGetter level,
      BlockPos pos,
      CollisionContext context) {
    return shapeForState(state);
  }

  @Override
  public VoxelShape getOcclusionShape(
      BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
    return Shapes.empty();
  }

  private static VoxelShape shapeForState(BlockState state) {
    String blockPath = TelecomToolSupport.resolveTelecomBlockPath(state);
    return blockPath.startsWith("nspga_") ? THIN_SHAPE : THICK_SHAPE;
  }

  @Override
  public InteractionResult use(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (player == null || !player.getItemInHand(hand).isEmpty()) {
      return InteractionResult.PASS;
    }
    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    String blockPath = TelecomToolSupport.resolveTelecomBlockPath(state);
    if (blockPath.isEmpty()) {
      return InteractionResult.PASS;
    }
    String endpoint = TelecomToolSupport.endpointId(level, pos, blockPath);
    String message =
        TelecomCommService.getInstance()
            .handleManualUse(endpoint, blockPath, player.isShiftKeyDown());
    player.displayClientMessage(new TextComponent("[Telecom] " + message), true);
    return InteractionResult.SUCCESS;
  }
}
