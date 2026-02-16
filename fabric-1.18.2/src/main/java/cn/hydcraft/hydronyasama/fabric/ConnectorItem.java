package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class ConnectorItem extends Item {
  public ConnectorItem(Properties properties) {
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
      TelecomToolSupport.notifyPlayer(context, "This block is not a telecom device");
      return InteractionResult.PASS;
    }

    TelecomCommService service = TelecomCommService.getInstance();
    String endpoint =
        TelecomToolSupport.endpointId(context.getLevel(), context.getClickedPos(), blockPath);
    service.ensureComponent(endpoint, blockPath);

    ItemStack stack = context.getItemInHand();
    if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
      TelecomToolSupport.clearConnectorState(stack);
      TelecomToolSupport.notifyPlayer(context, "Connector state cleared");
      return InteractionResult.SUCCESS;
    }

    CompoundTag tag = stack.getOrCreateTag();
    String source = tag.getString(TelecomToolSupport.SOURCE_ENDPOINT_TAG);
    if (source.isEmpty()) {
      tag.putString(TelecomToolSupport.SOURCE_ENDPOINT_TAG, endpoint);
      tag.putString(TelecomToolSupport.SOURCE_BLOCK_TAG, blockPath);
      TelecomToolSupport.notifyPlayer(context, "Source selected");
    } else if (source.equals(endpoint)) {
      TelecomToolSupport.clearConnectorState(stack);
      TelecomToolSupport.notifyPlayer(context, "Cancelled");
    } else {
      String sourceBlock = tag.getString(TelecomToolSupport.SOURCE_BLOCK_TAG);
      tag.putString(TelecomToolSupport.TARGET_ENDPOINT_TAG, endpoint);
      tag.putString(TelecomToolSupport.TARGET_BLOCK_TAG, blockPath);
      tag.putString(TelecomToolSupport.LINK_KEY_TAG, TelecomToolSupport.linkKey(source, endpoint));
      TelecomCommService.ConnectResult result =
          service.connect(source, sourceBlock, endpoint, blockPath);
      TelecomToolSupport.clearConnectorState(stack);
      if (result == TelecomCommService.ConnectResult.CONNECTED) {
        TelecomToolSupport.notifyPlayer(context, "Connected");
      } else if (result == TelecomCommService.ConnectResult.DISCONNECTED) {
        TelecomToolSupport.notifyPlayer(context, "Disconnected");
      } else {
        TelecomToolSupport.notifyPlayer(context, "Incompatible pair");
      }
    }

    service.tick();
    TelecomToolSupport.rememberClick(tag, blockPath, endpoint, context.getLevel());
    return InteractionResult.SUCCESS;
  }
}
