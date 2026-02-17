package cn.hydcraft.hydronyasama.fabric.network;

import cn.hydcraft.hydronyasama.create.CreateQueryGateway;
import cn.hydcraft.hydronyasama.create.CreateQueryRegistry;
import cn.hydcraft.hydronyasama.fabric.NgtCommandRegistrar;
import cn.hydcraft.hydronyasama.fabric.ProbeCommandRegistrar;
import cn.hydcraft.hydronyasama.fabric.TrainCommandRegistrar;
import cn.hydcraft.hydronyasama.fabric.mtr.FabricMtrQueryGateway;
import cn.hydcraft.hydronyasama.gateway.BeaconGatewayManager;
import cn.hydcraft.hydronyasama.mtr.MtrQueryGateway;
import cn.hydcraft.hydronyasama.mtr.MtrQueryRegistry;
import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import cn.hydcraft.hydronyasama.protocol.ChannelConstants;
import cn.hydcraft.hydronyasama.protocol.MessageSerializer;
import cn.hydcraft.hydronyasama.service.BeaconProviderService;
import cn.hydcraft.hydronyasama.service.BeaconServiceFactory;
import cn.hydcraft.hydronyasama.transport.ChannelMessageRouter;
import cn.hydcraft.hydronyasama.transport.ChannelMessenger;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Fabric 1.16.5 wiring for the hydroline beacon provider channel. */
public final class FabricBeaconNetwork {
  private static final ResourceLocation CHANNEL_ID =
      new ResourceLocation(ChannelConstants.CHANNEL_NAME);

  private final BeaconProviderService service;
  private final ChannelMessageRouter router;
  private final FabricChannelMessenger messenger;
  private final BeaconGatewayManager gatewayManager;

  public FabricBeaconNetwork() {
    this.service = BeaconServiceFactory.createDefault();
    this.messenger = new FabricChannelMessenger();
    this.router = new ChannelMessageRouter(service, messenger);
    this.gatewayManager = new BeaconGatewayManager(service);
    registerLifecycleHooks();
    registerChannelReceiver();
  }

  private void registerLifecycleHooks() {
    ServerLifecycleEvents.SERVER_STARTING.register(
        server -> {
          messenger.setServer(server);
          if (FabricLoader.getInstance().isModLoaded("mtr")) {
            MtrQueryRegistry.register(new FabricMtrQueryGateway(() -> server));
          } else {
            MtrQueryRegistry.register(MtrQueryGateway.UNAVAILABLE);
          }
          if (FabricLoader.getInstance().isModLoaded("create")) {
            // Fabric Create support pending
            CreateQueryRegistry.register(CreateQueryGateway.UNAVAILABLE);
          } else {
            CreateQueryRegistry.register(CreateQueryGateway.UNAVAILABLE);
          }
          ProbeCommandRegistrar.register(server);
          NgtCommandRegistrar.register(server);
          TrainCommandRegistrar.register(server);
          gatewayManager.start(FabricLoader.getInstance().getConfigDir());
        });
    ServerLifecycleEvents.SERVER_STOPPED.register(
        server -> {
          messenger.setServer(null);
          MtrQueryRegistry.register(MtrQueryGateway.UNAVAILABLE);
          gatewayManager.stop();
        });
  }

  private void registerChannelReceiver() {
    ServerPlayNetworking.registerGlobalReceiver(
        CHANNEL_ID,
        (server, player, handler, buf, responseSender) -> {
          byte[] bytes = new byte[buf.readableBytes()];
          buf.readBytes(bytes);
          server.execute(() -> router.handleIncoming(player.getUUID(), bytes));
        });
  }

  private static final class FabricChannelMessenger implements ChannelMessenger {
    private volatile MinecraftServer server;

    void setServer(MinecraftServer server) {
      this.server = server;
    }

    @Override
    public void reply(UUID playerUuid, BeaconResponse response) {
      MinecraftServer current = server;
      if (current == null) {
        return;
      }
      ServerPlayer player = current.getPlayerList().getPlayer(playerUuid);
      if (player == null) {
        return;
      }
      byte[] bytes = MessageSerializer.serialize(response);
      FriendlyByteBuf reply = PacketByteBufs.create();
      reply.writeBytes(bytes);
      ServerPlayNetworking.send(player, CHANNEL_ID, reply);
    }
  }
}
