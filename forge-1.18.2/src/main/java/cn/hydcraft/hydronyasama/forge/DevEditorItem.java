package cn.hydcraft.hydronyasama.forge;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

final class DevEditorItem extends Item {
  DevEditorItem(Properties properties) {
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

    String endpoint =
        TelecomToolSupport.endpointId(context.getLevel(), context.getClickedPos(), blockPath);
    CompoundTag tag = context.getItemInHand().getOrCreateTag();
    if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
      tag.putBoolean(
          TelecomToolSupport.EDITOR_INVERTER_TAG,
          !tag.getBoolean(TelecomToolSupport.EDITOR_INVERTER_TAG));
      TelecomToolSupport.notifyPlayer(
          context,
          "Inverter "
              + (tag.getBoolean(TelecomToolSupport.EDITOR_INVERTER_TAG) ? "enabled" : "disabled"));
    } else {
      int mode = tag.getInt(TelecomToolSupport.EDITOR_MODE_TAG);
      int nextMode = (mode + 1) % 3;
      tag.putInt(TelecomToolSupport.EDITOR_MODE_TAG, nextMode);
      TelecomToolSupport.notifyPlayer(context, "Editor mode=" + nextMode);
    }

    TelecomCommService service = TelecomCommService.getInstance();
    service.applyEditorState(
        endpoint,
        blockPath,
        tag.getInt(TelecomToolSupport.EDITOR_MODE_TAG),
        tag.getBoolean(TelecomToolSupport.EDITOR_INVERTER_TAG));
    service.tick();
    TelecomToolSupport.rememberClick(tag, blockPath, endpoint, context.getLevel());
    return InteractionResult.SUCCESS;
  }
}
