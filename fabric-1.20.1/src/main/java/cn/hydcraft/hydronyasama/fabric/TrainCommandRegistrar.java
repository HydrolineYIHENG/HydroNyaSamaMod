package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.core.train.TrainControlService;
import cn.hydcraft.hydronyasama.core.train.TrainControlState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class TrainCommandRegistrar {
  private TrainCommandRegistrar() {}

  public static void register(MinecraftServer server) {
    CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
    dispatcher.register(
        Commands.literal("trainctl")
            .then(
                Commands.argument("trainId", StringArgumentType.word())
                    .then(Commands.literal("show").executes(TrainCommandRegistrar::show))
                    .then(
                        Commands.literal("set")
                            .then(
                                Commands.argument("direction", IntegerArgumentType.integer(-1, 1))
                                    .then(
                                        Commands.argument(
                                                "power",
                                                IntegerArgumentType.integer(
                                                    0, TrainControlState.MAX_POWER))
                                            .then(
                                                Commands.argument(
                                                        "brake",
                                                        IntegerArgumentType.integer(
                                                            TrainControlState.MIN_BRAKE_RESISTANCE,
                                                            TrainControlState.MAX_BRAKE_RESISTANCE))
                                                    .executes(TrainCommandRegistrar::set)))))
                    .then(
                        Commands.literal("tick")
                            .executes(TrainCommandRegistrar::tickDefault)
                            .then(
                                Commands.argument("dt", DoubleArgumentType.doubleArg(0.01D, 2.0D))
                                    .executes(TrainCommandRegistrar::tickWithDt)))));
  }

  private static int show(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    String trainId = StringArgumentType.getString(context, "trainId");
    TrainControlState serverState = TrainControlService.getInstance().snapshotServer(trainId);
    TrainControlState clientState = TrainControlService.getInstance().snapshotClient(trainId);
    notify(
        context.getSource().getPlayerOrException(),
        "train="
            + trainId
            + " server[d="
            + serverState.getDirection()
            + ",p="
            + serverState.getPower()
            + ",b="
            + serverState.getBrakeResistance()
            + ",v="
            + format(serverState.getVelocity())
            + "] client[v="
            + format(clientState.getVelocity())
            + "]");
    return 1;
  }

  private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    String trainId = StringArgumentType.getString(context, "trainId");
    int direction = IntegerArgumentType.getInteger(context, "direction");
    int power = IntegerArgumentType.getInteger(context, "power");
    int brake = IntegerArgumentType.getInteger(context, "brake");
    TrainControlState state =
        TrainControlService.getInstance().applyControl(trainId, direction, power, brake, 0.0D);
    notify(
        context.getSource().getPlayerOrException(),
        "updated velocity=" + format(state.getVelocity()));
    return 1;
  }

  private static int tickDefault(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    return tickInternal(context, 0.1D);
  }

  private static int tickWithDt(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    return tickInternal(context, DoubleArgumentType.getDouble(context, "dt"));
  }

  private static int tickInternal(CommandContext<CommandSourceStack> context, double dt)
      throws CommandSyntaxException {
    String trainId = StringArgumentType.getString(context, "trainId");
    TrainControlState state =
        TrainControlService.getInstance()
            .tick(trainId, 40000.0D, 0.03D, 2400000.0D, 25.0D, 0.8D, 1.0D, dt);
    notify(
        context.getSource().getPlayerOrException(), "tick velocity=" + format(state.getVelocity()));
    return 1;
  }

  private static void notify(ServerPlayer player, String text) {
    player.displayClientMessage(Component.literal("[trainctl] " + text), false);
  }

  private static String format(double value) {
    return String.format(java.util.Locale.ROOT, "%.3f", value);
  }
}
