package cn.hydcraft.hydronyasama.forge;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

final class TelecomInteractiveBlock extends Block {
  private static final VoxelShape THIN_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);
  private static final VoxelShape THICK_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);

  TelecomInteractiveBlock(Properties properties) {
    super(properties);
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return shapeForState(state);
  }

  @Override
  public VoxelShape getCollisionShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return shapeForState(state);
  }

  @Override
  public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
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
    TelecomCommService.getInstance().handleManualUse(endpoint, blockPath, player.isShiftKeyDown());
    return InteractionResult.SUCCESS;
  }

  @Override
  public void neighborChanged(
      BlockState state,
      Level level,
      BlockPos pos,
      Block neighborBlock,
      BlockPos neighborPos,
      boolean movedByPiston) {
    super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    if (level.isClientSide) {
      return;
    }
    String blockPath = TelecomToolSupport.resolveTelecomBlockPath(state);
    if (!"signal_box_input".equals(blockPath)) {
      return;
    }
    String endpoint = TelecomToolSupport.endpointId(level, pos, blockPath);
    TelecomCommService.getInstance()
        .setInputFromRedstone(endpoint, blockPath, level.hasNeighborSignal(pos));
  }

  @Override
  public boolean canConnectRedstone(
      BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
    String blockPath = TelecomToolSupport.resolveTelecomBlockPath(state);
    return "signal_box_input".equals(blockPath) || "signal_box_output".equals(blockPath);
  }

  @Override
  public boolean isSignalSource(BlockState state) {
    return "signal_box_output".equals(TelecomToolSupport.resolveTelecomBlockPath(state));
  }

  @Override
  public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
    String blockPath = TelecomToolSupport.resolveTelecomBlockPath(state);
    if (!"signal_box_output".equals(blockPath) || !(level instanceof Level)) {
      return 0;
    }
    Level world = (Level) level;
    if (world.isClientSide) {
      return 0;
    }
    String endpoint = TelecomToolSupport.endpointId(world, pos, blockPath);
    return TelecomCommService.getInstance().redstoneOutputPowered(endpoint, blockPath) ? 15 : 0;
  }

  @Override
  public int getDirectSignal(
      BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
    return direction == Direction.UP ? getSignal(state, level, pos, direction) : 0;
  }
}
