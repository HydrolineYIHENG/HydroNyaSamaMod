package cn.hydcraft.hydronyasama.forge;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommRuntime;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomNgScriptEngine;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

final class NgTabletItem extends WritableBookItem {
  NgTabletItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!player.isShiftKeyDown()) {
      return super.use(level, player, hand);
    }
    if (level.isClientSide) {
      return InteractionResultHolder.success(stack);
    }
    CompoundTag tag = stack.getOrCreateTag();
    String code = TelecomToolSupport.readTabletCode(tag);
    List<String> logs =
        TelecomNgScriptEngine.run(
            code,
            tag.getString(TelecomToolSupport.LAST_ENDPOINT_TAG),
            tag.getString(TelecomToolSupport.LAST_BLOCK_TAG),
            TelecomCommService.getInstance());
    for (int i = 0; i < logs.size() && i < 8; i++) {
      TelecomToolSupport.notifyPlayer(player, logs.get(i));
    }
    return InteractionResultHolder.success(stack);
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
