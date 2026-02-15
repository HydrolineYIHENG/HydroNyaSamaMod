package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.fabric.content.FabricContentRegistrar;
import cn.hydcraft.hydronyasama.objrender.fabric.v120.ObjRenderClientBootstrap120;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public final class BeaconProviderFabricClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    new ObjRenderClientBootstrap120().initialize();
    if (FabricContentRegistrar.telecomRenderBlockEntityType() != null) {
      BlockEntityRendererRegistry.register(
          FabricContentRegistrar.telecomRenderBlockEntityType(),
          TelecomRenderBlockEntityRenderer::new);
    }
  }
}
