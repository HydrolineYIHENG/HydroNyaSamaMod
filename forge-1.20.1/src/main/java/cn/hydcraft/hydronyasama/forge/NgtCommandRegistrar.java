package cn.hydcraft.hydronyasama.forge;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomNgScriptEngine;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class NgtCommandRegistrar {
  private NgtCommandRegistrar() {}

  public static void register(MinecraftServer server) {
    CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
    dispatcher.register(
        Commands.literal("ngt")
            .executes(NgtCommandRegistrar::showStatus)
            .then(Commands.literal("run").executes(NgtCommandRegistrar::runCode))
            .then(
                Commands.literal("set")
                    .then(
                        Commands.argument("code", StringArgumentType.greedyString())
                            .executes(NgtCommandRegistrar::setCode)))
            .then(Commands.literal("clear").executes(NgtCommandRegistrar::clearCode)));
  }

  private static int showStatus(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    ItemStack stack = findTablet(context.getSource().getPlayerOrException());
    if (stack.isEmpty()) {
      return 0;
    }
    String code = TelecomToolSupport.readTabletCode(stack.getOrCreateTag());
    TelecomToolSupport.notifyPlayer(
        context.getSource().getPlayerOrException(),
        code.isEmpty() ? "NGT code is empty" : "NGT code ready");
    return 1;
  }

  private static int setCode(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    ServerPlayer player = context.getSource().getPlayerOrException();
    ItemStack stack = findTablet(player);
    if (stack.isEmpty()) {
      return 0;
    }
    String code = StringArgumentType.getString(context, "code").trim();
    stack.getOrCreateTag().putString(TelecomToolSupport.TABLET_CODE_TAG, code);
    TelecomToolSupport.notifyPlayer(
        player, code.isEmpty() ? "NGT code cleared" : "NGT code updated");
    return 1;
  }

  private static int clearCode(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    ServerPlayer player = context.getSource().getPlayerOrException();
    ItemStack stack = findTablet(player);
    if (stack.isEmpty()) {
      return 0;
    }
    stack.getOrCreateTag().remove(TelecomToolSupport.TABLET_CODE_TAG);
    TelecomToolSupport.notifyPlayer(player, "NGT code cleared");
    return 1;
  }

  private static int runCode(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    ServerPlayer player = context.getSource().getPlayerOrException();
    ItemStack stack = findTablet(player);
    if (stack.isEmpty()) {
      return 0;
    }
    String code = TelecomToolSupport.readTabletCode(stack.getOrCreateTag());
    List<String> logs =
        TelecomNgScriptEngine.run(
            code,
            stack.getOrCreateTag().getString(TelecomToolSupport.LAST_ENDPOINT_TAG),
            stack.getOrCreateTag().getString(TelecomToolSupport.LAST_BLOCK_TAG),
            TelecomCommService.getInstance());
    for (int i = 0; i < logs.size() && i < 8; i++) {
      TelecomToolSupport.notifyPlayer(player, logs.get(i));
    }
    return 1;
  }

  private static ItemStack findTablet(ServerPlayer player) {
    ItemStack mainHand = player.getMainHandItem();
    if (mainHand.getItem() instanceof NgTabletItem) {
      return mainHand;
    }
    ItemStack offHand = player.getOffhandItem();
    if (offHand.getItem() instanceof NgTabletItem) {
      return offHand;
    }
    TelecomToolSupport.notifyPlayer(player, "Hold NGTablet in main/off hand");
    return ItemStack.EMPTY;
  }
}
