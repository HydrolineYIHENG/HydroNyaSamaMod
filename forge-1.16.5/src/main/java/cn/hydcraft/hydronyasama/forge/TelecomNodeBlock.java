package cn.hydcraft.hydronyasama.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

final class TelecomNodeBlock extends BaseEntityBlock {
  static final BooleanProperty POWERED = BlockStateProperties.POWERED;

  TelecomNodeBlock(BlockBehaviour.Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(POWERED, Boolean.FALSE));
  }

  @Override
  public BlockEntity newBlockEntity(BlockGetter blockGetter) {
    return new TelecomNodeBlockEntity();
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public InteractionResult use(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hitResult) {
    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }

    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (!(blockEntity instanceof TelecomNodeBlockEntity)) {
      return InteractionResult.PASS;
    }

    TelecomNodeBlockEntity telecomNode = (TelecomNodeBlockEntity) blockEntity;
    if (player.isShiftKeyDown()) {
      telecomNode.cycleChannel();
    } else {
      boolean nextPowered = !state.getValue(POWERED);
      level.setBlock(pos, state.setValue(POWERED, nextPowered), 3);
      telecomNode.setPowered(nextPowered);
    }

    refreshSelfAndNeighbors(level, pos);
    return InteractionResult.SUCCESS;
  }

  @Override
  public void neighborChanged(
      BlockState state,
      Level level,
      BlockPos pos,
      Block block,
      BlockPos fromPos,
      boolean isMoving) {
    super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    if (!level.isClientSide()) {
      refreshNode(level, pos);
    }
  }

  @Override
  public void onPlace(
      BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    super.onPlace(state, level, pos, oldState, isMoving);
    if (!level.isClientSide() && state.getBlock() != oldState.getBlock()) {
      refreshSelfAndNeighbors(level, pos);
    }
  }

  @Override
  public void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
    if (!level.isClientSide() && state.getBlock() != newState.getBlock()) {
      refreshSelfAndNeighbors(level, pos);
    }
    super.onRemove(state, level, pos, newState, isMoving);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(POWERED);
  }

  private static void refreshSelfAndNeighbors(Level level, BlockPos pos) {
    refreshNode(level, pos);
    for (Direction direction : Direction.values()) {
      refreshNode(level, pos.relative(direction));
    }
  }

  private static void refreshNode(Level level, BlockPos pos) {
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity instanceof TelecomNodeBlockEntity) {
      ((TelecomNodeBlockEntity) blockEntity).rebuildTopology();
    }
  }
}
