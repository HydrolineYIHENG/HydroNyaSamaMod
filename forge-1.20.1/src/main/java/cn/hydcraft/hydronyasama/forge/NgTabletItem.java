package cn.hydcraft.hydronyasama.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

final class NgTabletItem extends Item {
  NgTabletItem(Properties properties) {
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
    tag.putString(TelecomToolSupport.TABLET_SCAN_TAG, endpoint);
    tag.putLong(TelecomToolSupport.TABLET_SCAN_TIME_TAG, System.currentTimeMillis());
    TelecomToolSupport.rememberClick(tag, blockPath, endpoint, context.getLevel());
    return InteractionResult.SUCCESS;
  }
}
