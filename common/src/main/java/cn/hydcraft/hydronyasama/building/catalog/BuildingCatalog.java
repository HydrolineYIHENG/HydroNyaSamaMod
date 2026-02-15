package cn.hydcraft.hydronyasama.building.catalog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Static baseline metadata used by the phased migration. */
public final class BuildingCatalog {
  public static final int LEGACY_HARD_BLOCK_COUNT = 64;
  public static final int LEGACY_SOFT_BLOCK_COUNT = 11;
  public static final int LEGACY_FAMILY_COUNT = BuildingFamily.values().length;
  public static final List<BuildingMaterialType> MATERIAL_TYPES =
      Collections.unmodifiableList(Arrays.asList(BuildingMaterialType.values()));

  private BuildingCatalog() {}
}
