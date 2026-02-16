package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.fabric.content.FabricContentRegistrar;
import cn.hydcraft.hydronyasama.fabric.config.HydroNyaSamaClientConfig;
import cn.hydcraft.hydronyasama.objrender.fabric.v120.ObjRenderClientBootstrap120;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BeaconProviderFabricClient implements ClientModInitializer {
  private static final Logger LOGGER = LogManager.getLogger("HydroNyaSama");

  @Override
  public void onInitializeClient() {
    HydroNyaSamaClientConfig.load();
    LOGGER.warn("[obj] canary mode enabled on Fabric 1.20.1 (single OBJ model)");
    new ObjRenderClientBootstrap120().initialize();
    if (FabricContentRegistrar.telecomRenderBlockEntityType() != null) {
      BlockEntityRendererRegistry.register(
          FabricContentRegistrar.telecomRenderBlockEntityType(),
          TelecomRenderBlockEntityRenderer::new);
    }
  }
}
