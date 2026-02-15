package cn.hydcraft.hydronyasama.core;

import cn.hydcraft.hydronyasama.BeaconProviderMod;

/** Bootstrap for migrated NyaSamaCore features. */
public final class CoreModule {
  private static volatile boolean initialized;

  private CoreModule() {}

  public static void init() {
    if (initialized) {
      return;
    }
    initialized = true;
    BeaconProviderMod.LOGGER.info("Core module baseline initialized");
  }
}
