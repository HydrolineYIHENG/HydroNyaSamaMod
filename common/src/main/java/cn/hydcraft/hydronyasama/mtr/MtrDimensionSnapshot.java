package cn.hydcraft.hydronyasama.mtr;

import java.util.Objects;
import mtr.data.DataCache;
import mtr.data.RailwayData;

/**
 * Immutable view describing a single Minecraft dimension and its associated RailwayData instance.
 */
public final class MtrDimensionSnapshot {
  private final String dimensionId;
  private final RailwayData railwayData;

  public MtrDimensionSnapshot(String dimensionId, RailwayData railwayData) {
    this.dimensionId = Objects.requireNonNull(dimensionId, "dimensionId");
    this.railwayData = Objects.requireNonNull(railwayData, "railwayData");
  }

  public String getDimensionId() {
    return dimensionId;
  }

  public RailwayData getRailwayData() {
    return railwayData;
  }

  /**
   * Ensures the associated {@link DataCache} is synchronized before data is read.
   *
   * @return the refreshed cache instance, or {@code null} if the railway data is missing a cache.
   */
  public DataCache refreshAndGetCache() {
    DataCache cache = railwayData.dataCache;
    return cache;
  }
}
