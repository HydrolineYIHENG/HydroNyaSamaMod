package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.objrender.fabric.v118.ObjRenderClientBootstrap118;
import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric client entrypoint for client-only render bootstrap.
 */
public final class BeaconProviderFabricClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    new ObjRenderClientBootstrap118().initialize();
  }
}
