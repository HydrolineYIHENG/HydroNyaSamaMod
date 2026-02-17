package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.fabric.content.FabricContentRegistrar;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommRuntime;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TelecomRenderBlockEntity extends BlockEntity {
  private static final String INPUT_TAG = "telecomInput";
  private static final String ENABLED_TAG = "telecomEnabled";
  private static final String OUTPUT_TAG = "telecomOutput";

  private boolean telecomInput;
  private boolean telecomEnabled;
  private boolean telecomOutput;

  public TelecomRenderBlockEntity(BlockPos pos, BlockState state) {
    super(FabricContentRegistrar.telecomRenderBlockEntityType(), pos, state);
  }

  public boolean telecomInput() {
    return telecomInput;
  }

  public boolean telecomEnabled() {
    return telecomEnabled;
  }

  public boolean telecomOutput() {
    return telecomOutput;
  }

  public static void serverTick(
      Level level, BlockPos pos, BlockState state, TelecomRenderBlockEntity blockEntity) {
    if (level == null || level.isClientSide) {
      return;
    }
    String blockPath = TelecomToolSupport.resolveTelecomBlockPath(state);
    if (blockPath.isEmpty()) {
      return;
    }
    String endpoint = TelecomToolSupport.endpointId(level, pos, blockPath);
    if ("signal_box_input".equals(blockPath)) {
      TelecomCommService.getInstance()
          .setInputFromRedstone(endpoint, blockPath, level.hasNeighborSignal(pos));
    }
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

  private void setPanelState(boolean input, boolean enabled, boolean output) {
    if (telecomInput == input && telecomEnabled == enabled && telecomOutput == output) {
      return;
    }
    boolean enabledChanged = telecomEnabled != enabled;
    telecomInput = input;
    telecomEnabled = enabled;
    telecomOutput = output;
    syncToClient();
    if (enabledChanged) {
      notifyRedstoneIfNeeded();
    }
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

  private void notifyRedstoneIfNeeded() {
    Level currentLevel = level;
    if (currentLevel == null || currentLevel.isClientSide) {
      return;
    }
    String blockPath = TelecomToolSupport.resolveTelecomBlockPath(getBlockState());
    if (!"signal_box_output".equals(blockPath)) {
      return;
    }
    currentLevel.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    currentLevel.updateNeighborsAt(worldPosition.below(), getBlockState().getBlock());
  }
}
