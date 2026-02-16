package cn.hydcraft.hydronyasama.forge;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

final class TelecomRenderBlock extends BaseEntityBlock {
  private static final VoxelShape THIN_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);
  private static final VoxelShape THICK_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);

  TelecomRenderBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    // Keep the base cube model visible and only overlay telemetry panel in BER.
    return RenderShape.MODEL;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TelecomRenderBlockEntity(pos, state);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> blockEntityType) {
    if (level == null || level.isClientSide) {
      return null;
    }
    return createTickerHelper(
        blockEntityType,
        ForgeContentRegistry.telecomRenderBlockEntityType(),
        TelecomRenderBlockEntity::serverTick);
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
    String message =
        TelecomCommService.getInstance()
            .handleManualUse(endpoint, blockPath, player.isShiftKeyDown());
    player.displayClientMessage(Component.literal("[Telecom] " + message), true);
    return InteractionResult.SUCCESS;
  }
}
