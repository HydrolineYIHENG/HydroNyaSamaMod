package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.fabric.config.HydroNyaSamaClientConfig;
import cn.hydcraft.hydronyasama.fabric.content.FabricContentRegistrar;
import cn.hydcraft.hydronyasama.objrender.fabric.v120.ObjRenderClientBootstrap120;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BeaconProviderFabricClient implements ClientModInitializer {
  private static final Logger LOGGER = LogManager.getLogger("HydroNyaSama");

  @Override
  public void onInitializeClient() {
    HydroNyaSamaClientConfig.load();
    LOGGER.warn("[obj] canary mode enabled on Fabric 1.20.1 (single OBJ model)");
    new ObjRenderClientBootstrap120().initialize();
    registerThinOpticsCutoutLayers();
    if (FabricContentRegistrar.telecomRenderBlockEntityType() != null) {
      BlockEntityRendererRegistry.register(
          FabricContentRegistrar.telecomRenderBlockEntityType(),
          TelecomRenderBlockEntityRenderer::new);
    }
    if (FabricContentRegistrar.opticsTextBlockEntityType() != null) {
      BlockEntityRendererRegistry.register(
          FabricContentRegistrar.opticsTextBlockEntityType(), OpticsTextBlockEntityRenderer::new);
    }
  }

  private static void registerThinOpticsCutoutLayers() {
    String[] cutoutIds = {
      "adsorption_lamp",
      "adsorption_lamp_mid",
      "adsorption_lamp_up",
      "fluorescent_lamp",
      "spot_light"
    };
    String[] translucentIds = {
      "text_wall",
      "text_wall_lit",
      "guide_board_np",
      "guide_board_sp",
      "guide_board_dp",
      "guide_board_np_lit",
      "guide_board_sp_lit",
      "guide_board_dp_lit"
    };
    for (String id : cutoutIds) {
      Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation("hydronyasama", id));
      if (block != Blocks.AIR) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
      }
    }
    for (String id : translucentIds) {
      Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation("hydronyasama", id));
      if (block != Blocks.AIR) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.translucent());
      }
    }
  }
}
