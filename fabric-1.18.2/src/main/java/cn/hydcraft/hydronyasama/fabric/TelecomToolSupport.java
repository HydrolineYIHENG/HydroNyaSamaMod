package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.content.LegacyContentIds;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

final class TelecomToolSupport {
  static final String SOURCE_ENDPOINT_TAG = "telecom_source_endpoint";
  static final String SOURCE_BLOCK_TAG = "telecom_source_block";
  static final String TARGET_ENDPOINT_TAG = "telecom_target_endpoint";
  static final String TARGET_BLOCK_TAG = "telecom_target_block";
  static final String LINK_KEY_TAG = "telecom_link_key";
  static final String LAST_ENDPOINT_TAG = "telecom_last_endpoint";
  static final String LAST_BLOCK_TAG = "telecom_last_block";
  static final String LAST_CLICKED_TICK_TAG = "telecom_last_clicked_tick";
  static final String EDITOR_MODE_TAG = "telecom_editor_mode";
  static final String EDITOR_INVERTER_TAG = "telecom_editor_inverter";
  static final String TABLET_SCAN_TAG = "telecom_tablet_scan";
  static final String TABLET_SCAN_TIME_TAG = "telecom_tablet_scan_time";

  private static final Set<String> TELECOM_BLOCK_IDS = new HashSet<String>();

  static {
    TELECOM_BLOCK_IDS.addAll(LegacyContentIds.TELECOM_BLOCK_IDS);
    TELECOM_BLOCK_IDS.add("telecom_node");
  }

  private TelecomToolSupport() {}

  static String resolveTelecomBlockPath(BlockState state) {
    if (state == null) {
      return "";
    }
    String descriptionId = state.getBlock().getDescriptionId();
    String prefix = "block.hydronyasama.";
    if (!descriptionId.startsWith(prefix)) {
      return "";
    }
    String path = descriptionId.substring(prefix.length());
    return TELECOM_BLOCK_IDS.contains(path) ? path : "";
  }

  static String endpointId(Level level, BlockPos pos, String blockPath) {
    String dimension = "unknown";
    if (level != null && level.dimension() != null && level.dimension().location() != null) {
      dimension = level.dimension().location().toString();
    }
    return dimension + ":" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ":" + blockPath;
  }

  static String linkKey(String sourceEndpoint, String targetEndpoint) {
    if (sourceEndpoint == null || sourceEndpoint.isEmpty()) {
      return targetEndpoint == null ? "" : targetEndpoint;
    }
    if (targetEndpoint == null || targetEndpoint.isEmpty()) {
      return sourceEndpoint;
    }
    return sourceEndpoint.compareTo(targetEndpoint) <= 0
        ? sourceEndpoint + "->" + targetEndpoint
        : targetEndpoint + "->" + sourceEndpoint;
  }

  static void rememberClick(CompoundTag tag, String blockPath, String endpoint, Level level) {
    tag.putString(LAST_BLOCK_TAG, blockPath);
    tag.putString(LAST_ENDPOINT_TAG, endpoint);
    if (level != null) {
      tag.putLong(LAST_CLICKED_TICK_TAG, level.getGameTime());
    }
  }

  static boolean isServerSide(UseOnContext context) {
    return context != null && context.getLevel() != null && !context.getLevel().isClientSide();
  }

  static void clearConnectorState(ItemStack stack) {
    CompoundTag tag = stack.getOrCreateTag();
    tag.remove(SOURCE_ENDPOINT_TAG);
    tag.remove(SOURCE_BLOCK_TAG);
    tag.remove(TARGET_ENDPOINT_TAG);
    tag.remove(TARGET_BLOCK_TAG);
    tag.remove(LINK_KEY_TAG);
  }

  static void notifyPlayer(UseOnContext context, String message) {
    if (context == null || context.getPlayer() == null || message == null || message.isEmpty()) {
      return;
    }
    context.getPlayer().displayClientMessage(new TextComponent("[Telecom] " + message), true);
  }
}
