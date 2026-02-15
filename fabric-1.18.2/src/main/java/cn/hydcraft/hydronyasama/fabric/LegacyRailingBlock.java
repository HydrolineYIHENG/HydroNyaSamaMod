package cn.hydcraft.hydronyasama.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Legacy railing/roof block based on fence with extra up/down booleans for model states. */
final class LegacyRailingBlock extends FenceBlock {
  static final BooleanProperty UP = BooleanProperty.create("up");
  static final BooleanProperty DOWN = BooleanProperty.create("down");

  private final boolean roofMode;

  LegacyRailingBlock(BlockBehaviour.Properties properties, boolean roofMode) {
    super(properties);
    this.roofMode = roofMode;
    registerDefaultState(
        defaultBlockState().setValue(UP, Boolean.FALSE).setValue(DOWN, Boolean.FALSE));
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockState state = super.getStateForPlacement(context);
    if (state == null) {
      return null;
    }
    return withVerticalState(state, context.getLevel(), context.getClickedPos());
  }

  @Override
  public BlockState updateShape(
      BlockState state,
      Direction direction,
      BlockState neighborState,
      LevelAccessor level,
      BlockPos currentPos,
      BlockPos neighborPos) {
    BlockState updated =
        super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    return withVerticalState(updated, level, currentPos);
  }

  @Override
  public boolean connectsTo(BlockState state, boolean isSideSolid, Direction direction) {
    Block block = state.getBlock();
    if (block instanceof LegacyRailingBlock || block instanceof FenceGateBlock) {
      return true;
    }
    return super.connectsTo(state, isSideSolid, direction);
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    if (roofMode && state.getValue(UP)) {
      return Shapes.block();
    }
    return super.getShape(state, level, pos, context);
  }

  @Override
  public VoxelShape getCollisionShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    if (roofMode && state.getValue(UP)) {
      return Shapes.block();
    }
    return super.getCollisionShape(state, level, pos, context);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(UP, DOWN);
  }

  private BlockState withVerticalState(BlockState state, LevelReader level, BlockPos pos) {
    boolean down = shouldHaveDown(level, pos);
    boolean up = roofMode ? shouldHaveRoofTop(level, pos) : down;
    return state.setValue(DOWN, down).setValue(UP, up);
  }

  private boolean shouldHaveDown(LevelReader level, BlockPos pos) {
    BlockPos belowPos = pos.below();
    BlockState belowState = level.getBlockState(belowPos);
    return belowState.isFaceSturdy(level, belowPos, Direction.UP)
        || belowState.getBlock() instanceof FenceBlock;
  }

  private boolean shouldHaveRoofTop(LevelReader level, BlockPos pos) {
    BlockState aboveState = level.getBlockState(pos.above());
    return !(aboveState.getBlock() instanceof FenceBlock);
  }
}
