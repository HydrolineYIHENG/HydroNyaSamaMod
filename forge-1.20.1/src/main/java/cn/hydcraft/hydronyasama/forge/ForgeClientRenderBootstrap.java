package cn.hydcraft.hydronyasama.forge;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
    modid = BeaconProviderMod.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT)
public final class ForgeClientRenderBootstrap {
  private ForgeClientRenderBootstrap() {}

  @SubscribeEvent
  public static void onClientSetup(final FMLClientSetupEvent event) {
    event.enqueueWork(
        () ->
            BlockEntityRenderers.register(
                ForgeContentRegistry.telecomRenderBlockEntityType(),
                TelecomRenderBlockEntityRenderer::new));
  }
}
