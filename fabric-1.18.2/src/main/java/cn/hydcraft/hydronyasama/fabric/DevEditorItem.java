package cn.hydcraft.hydronyasama.fabric;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class DevEditorItem extends Item {
  public DevEditorItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    if (!TelecomToolSupport.isServerSide(context)) {
      return InteractionResult.SUCCESS;
    }

    String blockPath =
        TelecomToolSupport.resolveTelecomBlockPath(
            context.getLevel().getBlockState(context.getClickedPos()));
    if (blockPath.isEmpty()) {
      return InteractionResult.PASS;
    }

    String endpoint =
        TelecomToolSupport.endpointId(context.getLevel(), context.getClickedPos(), blockPath);
    CompoundTag tag = context.getItemInHand().getOrCreateTag();
    if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
      tag.putBoolean(
          TelecomToolSupport.EDITOR_INVERTER_TAG,
          !tag.getBoolean(TelecomToolSupport.EDITOR_INVERTER_TAG));
    } else {
      int mode = tag.getInt(TelecomToolSupport.EDITOR_MODE_TAG);
      tag.putInt(TelecomToolSupport.EDITOR_MODE_TAG, (mode + 1) % 3);
    }
    TelecomToolSupport.rememberClick(tag, blockPath, endpoint, context.getLevel());
    return InteractionResult.SUCCESS;
  }
}


