package cn.hydcraft.hydronyasama.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ProbeCommandRegistrar {
  private ProbeCommandRegistrar() {}

  public static void register(MinecraftServer server) {
    CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
    dispatcher.register(
        Commands.literal("probeCmd")
            .requires(source -> source.hasPermission(2) || isCreativePlayer(source))
            .then(
                Commands.argument("command", StringArgumentType.greedyString())
                    .executes(ProbeCommandRegistrar::setCommand)));
  }

  private static int setCommand(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    ServerPlayer player = context.getSource().getPlayerOrException();
    ItemStack stack = player.getMainHandItem();
    if (stack.getItem() != FabricContentRegistry.probeItem()) {
      return 0;
    }

    String command = ProbeItem.sanitizeCommand(StringArgumentType.getString(context, "command"));
    if (command.isEmpty()) {
      return 0;
    }

    stack.getOrCreateTag().putString(ProbeItem.COMMAND_TAG, command);
    return 1;
  }

  private static boolean isCreativePlayer(CommandSourceStack source) {
    if (source.getEntity() instanceof ServerPlayer) {
      return ((ServerPlayer) source.getEntity()).isCreative();
    }
    return false;
  }
}
