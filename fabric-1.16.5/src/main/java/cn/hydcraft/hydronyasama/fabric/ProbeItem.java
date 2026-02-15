package cn.hydcraft.hydronyasama.fabric;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

final class ProbeItem extends Item {
  static final String COMMAND_TAG = "command";

  ProbeItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }

    if (!(context.getPlayer() instanceof ServerPlayer)) {
      return InteractionResult.PASS;
    }
    ServerPlayer player = (ServerPlayer) context.getPlayer();
    if (!player.isShiftKeyDown()) {
      return InteractionResult.PASS;
    }

    ItemStack stack = context.getItemInHand();
    CompoundTag tag = stack.getTag();
    if (tag == null) {
      return InteractionResult.SUCCESS;
    }

    String command = sanitizeCommand(tag.getString(COMMAND_TAG));
    if (command.isEmpty()) {
      return InteractionResult.SUCCESS;
    }

    executeAsPlayer(player, command);
    return InteractionResult.SUCCESS;
  }

  static String sanitizeCommand(String value) {
    if (value == null) {
      return "";
    }
    String sanitized = value.trim();
    if (sanitized.startsWith("/")) {
      sanitized = sanitized.substring(1).trim();
    }
    return sanitized;
  }

  private static void executeAsPlayer(ServerPlayer player, String command) {
    MinecraftServer server = player.getServer();
    if (server == null) {
      return;
    }
    try {
      server.getCommands().getDispatcher().execute(command, player.createCommandSourceStack());
    } catch (CommandSyntaxException ignored) {
    }
  }
}
