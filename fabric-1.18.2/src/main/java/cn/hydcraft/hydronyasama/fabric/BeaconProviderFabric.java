package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import cn.hydcraft.hydronyasama.core.content.ModContent;
import cn.hydcraft.hydronyasama.fabric.content.FabricContentRegistrar;
import cn.hydcraft.hydronyasama.fabric.network.FabricBeaconNetwork;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/** Fabric entrypoint delegating into the shared bootstrap. */
public final class BeaconProviderFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    BeaconProviderMod.init();
    ModContent.bootstrap(new FabricContentRegistrar());
    ServerTickEvents.END_SERVER_TICK.register(server -> TelecomCommService.getInstance().tick());
    ServerLifecycleEvents.SERVER_STOPPED.register(
        server -> TelecomCommService.getInstance().reset());
    new FabricBeaconNetwork();
  }
}
