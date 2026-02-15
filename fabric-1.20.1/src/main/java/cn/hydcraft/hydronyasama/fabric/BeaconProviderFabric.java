package cn.hydcraft.hydronyasama.fabric;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import cn.hydcraft.hydronyasama.core.content.ModContent;
import cn.hydcraft.hydronyasama.fabric.content.FabricContentRegistrar;
import cn.hydcraft.hydronyasama.fabric.network.FabricBeaconNetwork;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric entrypoint delegating into the shared bootstrap.
 */
public final class BeaconProviderFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BeaconProviderMod.init();
        ModContent.bootstrap(new FabricContentRegistrar());
        FabricContentRegistrar.finalizeTelecomRenderRegistry();
        new FabricBeaconNetwork();
    }
}
