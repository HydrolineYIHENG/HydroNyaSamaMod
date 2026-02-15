package cn.hydcraft.hydronyasama.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Legacy strip stair with quarter-width collision boxes matching old NyaSama behavior. */
public final class LegacyStripBlock extends StairBlock {
  private static final VoxelShape QTR_BOT_NORTH = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 8.0D, 16.0D);
  private static final VoxelShape QTR_BOT_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 8.0D);
  private static final VoxelShape QTR_BOT_WEST = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
  private static final VoxelShape QTR_BOT_EAST = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 8.0D, 16.0D);

  private static final VoxelShape QTR_TOP_NORTH = Block.box(0.0D, 8.0D, 8.0D, 16.0D, 16.0D, 16.0D);
  private static final VoxelShape QTR_TOP_SOUTH = Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 8.0D);
  private static final VoxelShape QTR_TOP_WEST = Block.box(8.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D);
  private static final VoxelShape QTR_TOP_EAST = Block.box(0.0D, 8.0D, 0.0D, 8.0D, 16.0D, 16.0D);

  private static final VoxelShape OCT_BOT_NW = Block.box(0.0D, 0.0D, 8.0D, 8.0D, 8.0D, 16.0D);
  private static final VoxelShape OCT_BOT_NE = Block.box(8.0D, 0.0D, 8.0D, 16.0D, 8.0D, 16.0D);
  private static final VoxelShape OCT_BOT_SW = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 8.0D, 8.0D);
  private static final VoxelShape OCT_BOT_SE = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 8.0D, 8.0D);

  private static final VoxelShape OCT_TOP_NW = Block.box(0.0D, 8.0D, 8.0D, 8.0D, 16.0D, 16.0D);
  private static final VoxelShape OCT_TOP_NE = Block.box(8.0D, 8.0D, 8.0D, 16.0D, 16.0D, 16.0D);
  private static final VoxelShape OCT_TOP_SW = Block.box(0.0D, 8.0D, 0.0D, 8.0D, 16.0D, 8.0D);
  private static final VoxelShape OCT_TOP_SE = Block.box(8.0D, 8.0D, 0.0D, 16.0D, 16.0D, 8.0D);
  public LegacyStripBlock(BlockState baseState, BlockBehaviour.Properties properties) {
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
      return quarterShape(state);
    }
    if (shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) {
      return Shapes.or(quarterShape(state), eighthShape(state));
    }
    return eighthShape(state);
  }

  private static VoxelShape quarterShape(BlockState state) {
    boolean bottom = state.getValue(HALF) == Half.BOTTOM;
    Direction direction = state.getValue(FACING).getOpposite();
    switch (direction) {
      case SOUTH:
        return bottom ? QTR_BOT_SOUTH : QTR_TOP_SOUTH;
      case WEST:
        return bottom ? QTR_BOT_WEST : QTR_TOP_WEST;
      case EAST:
        return bottom ? QTR_BOT_EAST : QTR_TOP_EAST;
      case NORTH:
      default:
        return bottom ? QTR_BOT_NORTH : QTR_TOP_NORTH;
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
    boolean bottom = state.getValue(HALF) == Half.BOTTOM;
    switch (direction) {
      case SOUTH:
        return bottom ? OCT_BOT_SE : OCT_TOP_SE;
      case WEST:
        return bottom ? OCT_BOT_SW : OCT_TOP_SW;
      case EAST:
        return bottom ? OCT_BOT_NE : OCT_TOP_NE;
      case NORTH:
      default:
        return bottom ? OCT_BOT_NW : OCT_TOP_NW;
    }
  }
}

