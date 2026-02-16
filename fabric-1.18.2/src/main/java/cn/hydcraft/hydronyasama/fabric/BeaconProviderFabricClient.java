package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.objrender.fabric.v118.ObjRenderClientBootstrap118;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Fabric client entrypoint for client-only render bootstrap.
 */
public final class BeaconProviderFabricClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    new ObjRenderClientBootstrap118().initialize();
    registerThinOpticsCutoutLayers();
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
      Block block = Registry.BLOCK.get(new ResourceLocation("hydronyasama", id));
      if (block != Blocks.AIR) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
      }
    }
    for (String id : translucentIds) {
      Block block = Registry.BLOCK.get(new ResourceLocation("hydronyasama", id));
      if (block != Blocks.AIR) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.translucent());
      }
    }
  }
}
