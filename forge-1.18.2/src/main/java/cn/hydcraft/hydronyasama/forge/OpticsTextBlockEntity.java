package cn.hydcraft.hydronyasama.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

final class OpticsTextBlockEntity extends BlockEntity {
  private static final String TEXT_TAG = "Text";
  private String text = "";

  OpticsTextBlockEntity(BlockPos pos, BlockState state) {
    super(ForgeContentRegistry.opticsTextBlockEntityType(), pos, state);
  }

  String text() {
    return text;
  }

  void setText(String text) {
    String normalized = normalize(text);
    if (normalized.equals(this.text)) {
      return;
    }
    this.text = normalized;
    setChanged();
    if (level != null) {
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putString(TEXT_TAG, text);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    text = normalize(tag.getString(TEXT_TAG));
  }

  private static String normalize(String text) {
    if (text == null) {
      return "";
    }
    String value = text.replaceAll("\\p{Cntrl}", "").trim();
    return value.length() > 64 ? value.substring(0, 64) : value;
  }
}
