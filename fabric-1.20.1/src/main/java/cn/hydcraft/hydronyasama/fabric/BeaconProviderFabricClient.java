package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.fabric.content.FabricContentRegistrar;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public final class BeaconProviderFabricClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    if (FabricContentRegistrar.telecomRenderBlockEntityType() != null) {
      BlockEntityRendererRegistry.register(
          FabricContentRegistrar.telecomRenderBlockEntityType(),
          TelecomRenderBlockEntityRenderer::new);
    }
  }
}
