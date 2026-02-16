package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommRuntime;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class NgTabletItem extends Item {
  public NgTabletItem(Properties properties) {
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
    TelecomCommService service = TelecomCommService.getInstance();
    TelecomCommRuntime.Snapshot snapshot = service.snapshot(endpoint, blockPath);
    service.tick();

    String state =
        snapshot == null
            ? "missing"
            : ("kind="
                + snapshot.kind
                + ";enabled="
                + snapshot.enabled
                + ";input="
                + snapshot.input
                + ";output="
                + snapshot.output
                + ";sender="
                + (snapshot.senderId == null ? "" : snapshot.senderId)
                + ";target="
                + (snapshot.targetId == null ? "" : snapshot.targetId)
                + ";transceiver="
                + (snapshot.transceiverId == null ? "" : snapshot.transceiverId));

    tag.putString(TelecomToolSupport.TABLET_SCAN_TAG, endpoint);
    tag.putLong(TelecomToolSupport.TABLET_SCAN_TIME_TAG, System.currentTimeMillis());
    tag.putString("telecom_tablet_scan_state", state);
    TelecomToolSupport.notifyPlayer(context, state);
    TelecomToolSupport.rememberClick(tag, blockPath, endpoint, context.getLevel());
    return InteractionResult.SUCCESS;
  }
}
