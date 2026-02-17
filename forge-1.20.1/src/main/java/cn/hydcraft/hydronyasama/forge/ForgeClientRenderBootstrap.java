package cn.hydcraft.hydronyasama.forge;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(
    modid = BeaconProviderMod.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT)
public final class ForgeClientRenderBootstrap {
  private static final String[] CUTOUT_IDS = {
    "adsorption_lamp", "adsorption_lamp_mid", "adsorption_lamp_up", "fluorescent_lamp", "spot_light"
  };

  private static final String[] TRANSLUCENT_IDS = {
    "text_wall",
    "text_wall_lit",
    "guide_board_np",
    "guide_board_sp",
    "guide_board_dp",
    "guide_board_np_lit",
    "guide_board_sp_lit",
    "guide_board_dp_lit"
  };

  private ForgeClientRenderBootstrap() {}

  @SubscribeEvent
  public static void onClientSetup(final FMLClientSetupEvent event) {
    event.enqueueWork(
        () -> {
          BlockEntityRenderers.register(
              ForgeContentRegistry.telecomRenderBlockEntityType(),
              TelecomRenderBlockEntityRenderer::new);
          BlockEntityRenderers.register(
              ForgeContentRegistry.opticsTextBlockEntityType(), OpticsTextBlockEntityRenderer::new);
          registerOpticsRenderLayers();
        });
  }

  private static void registerOpticsRenderLayers() {
    for (String id : CUTOUT_IDS) {
      Block block =
          ForgeRegistries.BLOCKS.getValue(new ResourceLocation(BeaconProviderMod.MOD_ID, id));
      if (block != null) {
        ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
      }
    }
    for (String id : TRANSLUCENT_IDS) {
      Block block =
          ForgeRegistries.BLOCKS.getValue(new ResourceLocation(BeaconProviderMod.MOD_ID, id));
      if (block != null) {
        ItemBlockRenderTypes.setRenderLayer(block, RenderType.translucent());
      }
    }
  }
}
