package cn.hydcraft.hydronyasama.forge;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommRuntime;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

final class TelecomRenderBlockEntity extends BlockEntity {
  private static final String INPUT_TAG = "telecomInput";
  private static final String ENABLED_TAG = "telecomEnabled";
  private static final String OUTPUT_TAG = "telecomOutput";

  private boolean telecomInput;
  private boolean telecomEnabled;
  private boolean telecomOutput;

  TelecomRenderBlockEntity(BlockPos pos, BlockState state) {
    super(ForgeContentRegistry.telecomRenderBlockEntityType(), pos, state);
  }

  boolean telecomInput() {
    return telecomInput;
  }

  boolean telecomEnabled() {
    return telecomEnabled;
  }

  boolean telecomOutput() {
    return telecomOutput;
  }

  static void serverTick(
      Level level, BlockPos pos, BlockState state, TelecomRenderBlockEntity blockEntity) {
    if (level == null || level.isClientSide) {
      return;
    }
    String blockPath = TelecomToolSupport.resolveTelecomBlockPath(state);
    if (blockPath.isEmpty()) {
      return;
    }
    String endpoint = TelecomToolSupport.endpointId(level, pos, blockPath);
    TelecomCommRuntime.Snapshot snapshot =
        TelecomCommService.getInstance().snapshot(endpoint, blockPath);
    if (snapshot != null) {
      blockEntity.setPanelState(snapshot.input, snapshot.enabled, snapshot.output);
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    writeSyncTag(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    readSyncTag(tag);
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag tag = super.getUpdateTag();
    writeSyncTag(tag);
    return tag;
  }

  @Override
  public Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
    CompoundTag tag = packet.getTag();
    if (tag != null) {
      readSyncTag(tag);
    }
  }

  private void setPanelState(boolean input, boolean enabled, boolean output) {
    if (telecomInput == input && telecomEnabled == enabled && telecomOutput == output) {
      return;
    }
    telecomInput = input;
    telecomEnabled = enabled;
    telecomOutput = output;
    syncToClient();
  }

  private void writeSyncTag(CompoundTag tag) {
    tag.putBoolean(INPUT_TAG, telecomInput);
    tag.putBoolean(ENABLED_TAG, telecomEnabled);
    tag.putBoolean(OUTPUT_TAG, telecomOutput);
  }

  private void readSyncTag(CompoundTag tag) {
    telecomInput = tag.getBoolean(INPUT_TAG);
    telecomEnabled = tag.getBoolean(ENABLED_TAG);
    telecomOutput = tag.getBoolean(OUTPUT_TAG);
  }

  private void syncToClient() {
    setChanged();
    Level currentLevel = level;
    if (currentLevel != null && !currentLevel.isClientSide) {
      BlockState state = getBlockState();
      currentLevel.sendBlockUpdated(worldPosition, state, state, 3);
    }
  }
}
