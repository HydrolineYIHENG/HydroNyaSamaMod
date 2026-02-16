package cn.hydcraft.hydronyasama.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Legacy edge block with facing + half(top/bottom/tall) states. */
public final class LegacyEdgeBlock extends HorizontalDirectionalBlock {
  static final EnumProperty<EdgeHalf> HALF = EnumProperty.create("half", EdgeHalf.class);

  private static final VoxelShape NORTH_BOTTOM = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 8.0D, 16.0D);
  private static final VoxelShape NORTH_TOP = Block.box(0.0D, 8.0D, 6.0D, 16.0D, 16.0D, 16.0D);
  private static final VoxelShape NORTH_TALL = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 16.0D);

  private static final VoxelShape SOUTH_BOTTOM = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 10.0D);
  private static final VoxelShape SOUTH_TOP = Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 10.0D);
  private static final VoxelShape SOUTH_TALL = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 10.0D);

  private static final VoxelShape WEST_BOTTOM = Block.box(6.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
  private static final VoxelShape WEST_TOP = Block.box(6.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D);
  private static final VoxelShape WEST_TALL = Block.box(6.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

  private static final VoxelShape EAST_BOTTOM = Block.box(0.0D, 0.0D, 0.0D, 10.0D, 8.0D, 16.0D);
  private static final VoxelShape EAST_TOP = Block.box(0.0D, 8.0D, 0.0D, 10.0D, 16.0D, 16.0D);
  private static final VoxelShape EAST_TALL = Block.box(0.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

  public LegacyEdgeBlock(BlockBehaviour.Properties properties) {
    super(properties);
    registerDefaultState(
        stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, EdgeHalf.BOTTOM));
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    Direction facing = context.getHorizontalDirection().getOpposite();
    Direction clickedFace = context.getClickedFace();
    double hitY = context.getClickLocation().y - context.getClickedPos().getY();
    EdgeHalf half =
        (clickedFace == Direction.DOWN || (clickedFace != Direction.UP && hitY > 0.5D))
            ? EdgeHalf.TOP
            : EdgeHalf.BOTTOM;
    return defaultBlockState().setValue(FACING, facing).setValue(HALF, half);
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    Direction direction = state.getValue(FACING);
    EdgeHalf half = state.getValue(HALF);
    switch (direction) {
      case SOUTH:
        return half == EdgeHalf.TOP
            ? SOUTH_TOP
            : (half == EdgeHalf.TALL ? SOUTH_TALL : SOUTH_BOTTOM);
      case WEST:
        return half == EdgeHalf.TOP ? WEST_TOP : (half == EdgeHalf.TALL ? WEST_TALL : WEST_BOTTOM);
      case EAST:
        return half == EdgeHalf.TOP ? EAST_TOP : (half == EdgeHalf.TALL ? EAST_TALL : EAST_BOTTOM);
      case NORTH:
      default:
        return half == EdgeHalf.TOP
            ? NORTH_TOP
            : (half == EdgeHalf.TALL ? NORTH_TALL : NORTH_BOTTOM);
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
    builder.add(FACING, HALF);
  }

  enum EdgeHalf implements StringRepresentable {
    TOP("top"),
    BOTTOM("bottom"),
    TALL("tall");

    private final String name;

    EdgeHalf(String name) {
      this.name = name;
    }

    @Override
    public String getSerializedName() {
      return name;
    }
  }
}
