package cn.hydcraft.hydronyasama.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

final class ConnectorItem extends Item {
  ConnectorItem(Properties properties) {
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

    ItemStack stack = context.getItemInHand();
    if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
      TelecomToolSupport.clearConnectorState(stack);
      return InteractionResult.SUCCESS;
    }

    String endpoint =
        TelecomToolSupport.endpointId(context.getLevel(), context.getClickedPos(), blockPath);
    CompoundTag tag = stack.getOrCreateTag();
    String source = tag.getString(TelecomToolSupport.SOURCE_ENDPOINT_TAG);
    if (source.isEmpty()) {
      tag.putString(TelecomToolSupport.SOURCE_ENDPOINT_TAG, endpoint);
      tag.putString(TelecomToolSupport.SOURCE_BLOCK_TAG, blockPath);
    } else if (!source.equals(endpoint)) {
      tag.putString(TelecomToolSupport.TARGET_ENDPOINT_TAG, endpoint);
      tag.putString(TelecomToolSupport.TARGET_BLOCK_TAG, blockPath);
      tag.putString(TelecomToolSupport.LINK_KEY_TAG, TelecomToolSupport.linkKey(source, endpoint));
    }
    TelecomToolSupport.rememberClick(tag, blockPath, endpoint, context.getLevel());
    return InteractionResult.SUCCESS;
  }
}
