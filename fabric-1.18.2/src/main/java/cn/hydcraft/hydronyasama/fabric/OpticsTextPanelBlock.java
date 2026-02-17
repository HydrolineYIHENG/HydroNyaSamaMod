package cn.hydcraft.hydronyasama.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class OpticsTextPanelBlock extends BaseEntityBlock {
  public OpticsTextPanelBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new OpticsTextBlockEntity(pos, state);
  }

  @Override
  public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
    return Shapes.empty();
  }

  @Override
  public VoxelShape getVisualShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return Shapes.empty();
  }

  @Override
  public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
    return true;
  }

  @Override
  public InteractionResult use(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (!(blockEntity instanceof OpticsTextBlockEntity)) {
      return InteractionResult.PASS;
    }
    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    OpticsTextBlockEntity textEntity = (OpticsTextBlockEntity) blockEntity;
    String current = textEntity.text();
    if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
      textEntity.setText("");
      player.displayClientMessage(new TextComponent("Optics text cleared"), true);
      return InteractionResult.SUCCESS;
    }
    if (player.getItemInHand(hand).hasCustomHoverName()) {
      String next = player.getItemInHand(hand).getHoverName().getString();
      textEntity.setText(next);
      player.displayClientMessage(new TextComponent("Optics text updated"), true);
      return InteractionResult.SUCCESS;
    }
    if (!current.isEmpty()) {
      player.displayClientMessage(new TextComponent("Text: " + current), true);
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }
}
