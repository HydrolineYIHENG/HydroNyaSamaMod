package cn.hydcraft.hydronyasama.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Legacy vertical-slab stair with full-height quarter/eighth collision boxes. */
final class LegacyVSlabBlock extends StairBlock {
  private static final VoxelShape QTR_NORTH = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
  private static final VoxelShape QTR_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
  private static final VoxelShape QTR_WEST = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
  private static final VoxelShape QTR_EAST = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);

  private static final VoxelShape OCT_NW = Block.box(0.0D, 0.0D, 8.0D, 8.0D, 16.0D, 16.0D);
  private static final VoxelShape OCT_NE = Block.box(8.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
  private static final VoxelShape OCT_SW = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 8.0D);
  private static final VoxelShape OCT_SE = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);

  LegacyVSlabBlock(BlockState baseState, BlockBehaviour.Properties properties) {
    super(baseState, properties);
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return legacyShape(state);
  }

  @Override
  public VoxelShape getCollisionShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return legacyShape(state);
  }

  private static VoxelShape legacyShape(BlockState state) {
    StairsShape shape = state.getValue(SHAPE);
    if (shape == StairsShape.STRAIGHT) {
      return quarterShape(state.getValue(FACING).getOpposite());
    }
    if (shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) {
      return Shapes.or(quarterShape(state.getValue(FACING).getOpposite()), eighthShape(state));
    }
    return eighthShape(state);
  }

  private static VoxelShape quarterShape(Direction direction) {
    switch (direction) {
      case SOUTH:
        return QTR_SOUTH;
      case WEST:
        return QTR_WEST;
      case EAST:
        return QTR_EAST;
      case NORTH:
      default:
        return QTR_NORTH;
    }
  }

  private static VoxelShape eighthShape(BlockState state) {
    Direction facing = state.getValue(FACING);
    Direction rotatedFacing;
    switch (state.getValue(SHAPE)) {
      case OUTER_RIGHT:
        rotatedFacing = facing.getClockWise();
        break;
      case INNER_RIGHT:
        rotatedFacing = facing.getOpposite();
        break;
      case INNER_LEFT:
        rotatedFacing = facing.getCounterClockWise();
        break;
      case OUTER_LEFT:
      default:
        rotatedFacing = facing;
        break;
    }

    Direction direction = rotatedFacing.getOpposite();
    switch (direction) {
      case SOUTH:
        return OCT_SE;
      case WEST:
        return OCT_SW;
      case EAST:
        return OCT_NE;
      case NORTH:
      default:
        return OCT_NW;
    }
  }
}
