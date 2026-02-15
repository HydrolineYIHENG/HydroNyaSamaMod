package cn.hydcraft.hydronyasama;

import cn.hydcraft.hydronyasama.building.BuildingModule;
import cn.hydcraft.hydronyasama.core.CoreModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared bootstrap used by every loader-specific entrypoint. */
public final class BeaconProviderMod {
  public static final String MOD_ID = "hydronyasama";
  public static final String MOD_NAME = "HydroNyaSama";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
  private static final String VERSION = resolveVersion();

  private BeaconProviderMod() {}

  public static void init() {
    LOGGER.info("Loaded {}", MOD_NAME);
    CoreModule.init();
    BuildingModule.init();
  }

  public static String getVersion() {
    return VERSION;
  }

  private static String resolveVersion() {
    Package pkg = BeaconProviderMod.class.getPackage();
    if (pkg != null && pkg.getImplementationVersion() != null) {
      return pkg.getImplementationVersion();
    }
    return "dev";
  }
}
