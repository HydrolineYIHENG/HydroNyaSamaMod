package cn.hydcraft.hydronyasama.forge;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

final class TelecomNodeBlockEntity extends BlockEntity {
  private static final String CHANNEL_TAG = "channel";
  private static final String LINK_COUNT_TAG = "linkCount";
  private static final String POWERED_TAG = "powered";

  private int channel = 1;
  private int linkCount;
  private boolean powered;

  TelecomNodeBlockEntity() {
    super(ForgeContentRegistry.telecomNodeBlockEntityType());
  }

  boolean isPowered() {
    return powered;
  }

  void setPowered(boolean nextPowered) {
    if (powered == nextPowered) {
      return;
    }
    powered = nextPowered;
    syncToClient();
  }

  void cycleChannel() {
    channel = channel >= 8 ? 1 : channel + 1;
    rebuildTopology();
  }

  void rebuildTopology() {
    Level currentLevel = level;
    if (currentLevel == null) {
      return;
    }

    int links = 0;
    for (Direction direction : Direction.values()) {
      BlockEntity otherEntity = currentLevel.getBlockEntity(worldPosition.relative(direction));
      if (otherEntity instanceof TelecomNodeBlockEntity) {
        TelecomNodeBlockEntity other = (TelecomNodeBlockEntity) otherEntity;
        if (other.channel == channel) {
          links++;
        }
      }
    }

    if (links != linkCount) {
      linkCount = links;
      syncToClient();
    }
  }

  @Override
  public CompoundTag save(CompoundTag tag) {
    CompoundTag saved = super.save(tag);
    writeSyncTag(saved);
    return saved;
  }

  @Override
  public void load(BlockState state, CompoundTag tag) {
    super.load(state, tag);
    readSyncTag(tag);
  }

  public CompoundTag getUpdateTag() {
    CompoundTag tag = new CompoundTag();
    writeSyncTag(tag);
    return tag;
  }

  public void handleUpdateTag(CompoundTag tag) {
    readSyncTag(tag);
  }

  public void handleUpdateTag(BlockState state, CompoundTag tag) {
    readSyncTag(tag);
  }

  private void writeSyncTag(CompoundTag tag) {
    tag.putInt(CHANNEL_TAG, channel);
    tag.putInt(LINK_COUNT_TAG, linkCount);
    tag.putBoolean(POWERED_TAG, powered);
  }

  private void readSyncTag(CompoundTag tag) {
    channel = Math.max(1, tag.getInt(CHANNEL_TAG));
    linkCount = Math.max(0, tag.getInt(LINK_COUNT_TAG));
    powered = tag.getBoolean(POWERED_TAG);
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
