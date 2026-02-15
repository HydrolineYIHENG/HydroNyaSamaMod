package cn.hydcraft.hydronyasama.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Legacy quarter-column strip block with horizontal facing. */
public final class LegacyVStripBlock extends HorizontalDirectionalBlock {
  private static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 8.0D);
  private static final VoxelShape SOUTH_SHAPE = Block.box(8.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
  private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 0.0D, 8.0D, 8.0D, 16.0D, 16.0D);
  private static final VoxelShape EAST_SHAPE = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
  public LegacyVStripBlock(BlockBehaviour.Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    Direction direction = state.getValue(FACING);
    switch (direction) {
      case SOUTH:
        return SOUTH_SHAPE;
      case WEST:
        return WEST_SHAPE;
      case EAST:
        return EAST_SHAPE;
      case NORTH:
      default:
        return NORTH_SHAPE;
    }
  }

  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
  }

  @Override
  public BlockState mirror(BlockState state, Mirror mirror) {
    return state.rotate(mirror.getRotation(state.getValue(FACING)));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }
}

