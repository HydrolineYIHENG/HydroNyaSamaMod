package cn.hydcraft.hydronyasama.forge;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import cn.hydcraft.hydronyasama.core.train.TrainControlService;
import cn.hydcraft.hydronyasama.forge.network.ForgeBeaconNetwork;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Forge bootstrap that wires shared logic and keeps room for Forge-only hooks. */
@Mod(BeaconProviderMod.MOD_ID)
public final class BeaconProviderForge {
  public BeaconProviderForge() {
    BeaconProviderMod.init();
    IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    ForgeContentRegistry.register(modBus);
    ForgeCreativeTabs.register(modBus);
    MinecraftForge.EVENT_BUS.register(this);
    new ForgeBeaconNetwork();
    modBus.addListener(this::onCommonSetup);
  }

  private void onCommonSetup(final FMLCommonSetupEvent event) {
    // Register Forge specifics here when needed.
  }

  @SubscribeEvent
  public void onServerTick(TickEvent.ServerTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      TelecomCommService.getInstance().tick();
    }
  }

  @SubscribeEvent
  public void onServerStopped(ServerStoppedEvent event) {
    TelecomCommService.getInstance().reset();
    TrainControlService.getInstance().reset();
  }
}
