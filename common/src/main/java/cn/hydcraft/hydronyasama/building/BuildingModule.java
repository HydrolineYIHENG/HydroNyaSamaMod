package cn.hydcraft.hydronyasama.building;

import cn.hydcraft.hydronyasama.BeaconProviderMod;

/** Bootstrap for migrated NyaSamaBuilding features. */
public final class BuildingModule {
  private static volatile boolean initialized;

  private BuildingModule() {}

  public static void init() {
    if (initialized) {
      return;
    }
    initialized = true;
    BeaconProviderMod.LOGGER.info("Building module baseline initialized");
  }
}
