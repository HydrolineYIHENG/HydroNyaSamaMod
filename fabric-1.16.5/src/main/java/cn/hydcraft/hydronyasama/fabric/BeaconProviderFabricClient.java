package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.objrender.fabric.v116.ObjRenderClientBootstrap116;
import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric client entrypoint for client-only render bootstrap.
 */
public final class BeaconProviderFabricClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    new ObjRenderClientBootstrap116().initialize();
  }
}
