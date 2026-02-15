package cn.hydcraft.hydronyasama.forge.mtr;

import cn.hydcraft.hydronyasama.mtr.MtrDataMapper;
import cn.hydcraft.hydronyasama.mtr.MtrDimensionSnapshot;
import cn.hydcraft.hydronyasama.mtr.MtrModels.DepotInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.DimensionOverview;
import cn.hydcraft.hydronyasama.mtr.MtrModels.FareAreaInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.NodePage;
import cn.hydcraft.hydronyasama.mtr.MtrModels.RouteDetail;
import cn.hydcraft.hydronyasama.mtr.MtrModels.StationInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.StationTimetable;
import cn.hydcraft.hydronyasama.mtr.MtrModels.TrainStatus;
import cn.hydcraft.hydronyasama.mtr.MtrQueryGateway;
import cn.hydcraft.hydronyasama.mtr.MtrRailwayDataAccess;
import cn.hydcraft.hydronyasama.mtr.MtrSnapshotCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import mtr.data.RailwayData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ForgeMtrQueryGateway implements MtrQueryGateway {
  private static final Logger LOGGER = LoggerFactory.getLogger(ForgeMtrQueryGateway.class);
  private final Supplier<MinecraftServer> serverSupplier;
  private static final long SNAPSHOT_CACHE_TTL_MILLIS = 1000L;
  private final MtrSnapshotCache snapshotCache;

  public ForgeMtrQueryGateway(Supplier<MinecraftServer> serverSupplier) {
    this.serverSupplier = serverSupplier;
    this.snapshotCache = new MtrSnapshotCache(this::captureSnapshotsNow, SNAPSHOT_CACHE_TTL_MILLIS);
  }

  @Override
  public boolean isReady() {
    return !captureSnapshots().isEmpty();
  }

  @Override
  public List<DimensionOverview> fetchNetworkOverview() {
    return MtrDataMapper.buildNetworkOverview(captureSnapshots());
  }

  @Override
  public Optional<RouteDetail> fetchRouteDetail(String dimensionId, long routeId) {
    List<MtrDimensionSnapshot> snapshots = captureSnapshots();
    return findSnapshot(snapshots, dimensionId)
        .flatMap(snapshot -> MtrDataMapper.buildRouteDetail(snapshot, routeId));
  }

  @Override
  public List<DepotInfo> fetchDepots(String dimensionId) {
    List<MtrDimensionSnapshot> snapshots = captureSnapshots();
    if (dimensionId == null || dimensionId.isEmpty()) {
      return snapshots.stream()
          .flatMap(snapshot -> MtrDataMapper.buildDepots(snapshot).stream())
          .collect(Collectors.toList());
    }
    return findSnapshot(snapshots, dimensionId)
        .map(MtrDataMapper::buildDepots)
        .orElseGet(Collections::emptyList);
  }

  @Override
  public List<FareAreaInfo> fetchFareAreas(String dimensionId) {
    List<MtrDimensionSnapshot> snapshots = captureSnapshots();
    return findSnapshot(snapshots, dimensionId)
        .map(MtrDataMapper::buildFareAreas)
        .orElseGet(Collections::emptyList);
  }

  @Override
  public NodePage fetchNodes(String dimensionId, String cursor, int limit) {
    List<MtrDimensionSnapshot> snapshots = captureSnapshots();
    return findSnapshot(snapshots, dimensionId)
        .map(snapshot -> MtrDataMapper.buildNodePage(snapshot, cursor, limit))
        .orElseGet(
            () ->
                new NodePage(
                    dimensionId == null ? "" : dimensionId, Collections.emptyList(), null));
  }

  @Override
  public Optional<StationTimetable> fetchStationTimetable(
      String dimensionId, long stationId, Long platformId) {
    List<MtrDimensionSnapshot> snapshots = captureSnapshots();
    return findSnapshot(snapshots, dimensionId)
        .flatMap(snapshot -> MtrDataMapper.buildStationTimetable(snapshot, stationId, platformId));
  }

  @Override
  public List<StationInfo> fetchStations(String dimensionId) {
    List<MtrDimensionSnapshot> snapshots = captureSnapshots();
    if (dimensionId == null || dimensionId.isEmpty()) {
      return snapshots.stream()
          .flatMap(snapshot -> MtrDataMapper.buildStations(snapshot).stream())
          .collect(Collectors.toList());
    }
    return findSnapshot(snapshots, dimensionId)
        .map(MtrDataMapper::buildStations)
        .orElseGet(Collections::emptyList);
  }

  @Override
  public List<TrainStatus> fetchRouteTrains(String dimensionId, long routeId) {
    List<MtrDimensionSnapshot> snapshots = captureSnapshots();
    return snapshots.stream()
        .filter(
            snapshot ->
                dimensionId == null
                    || dimensionId.isEmpty()
                    || snapshot.getDimensionId().equals(dimensionId))
        .flatMap(snapshot -> MtrDataMapper.buildRouteTrains(snapshot, routeId).stream())
        .collect(Collectors.toList());
  }

  @Override
  public List<TrainStatus> fetchDepotTrains(String dimensionId, long depotId) {
    List<MtrDimensionSnapshot> snapshots = captureSnapshots();
    return findSnapshot(snapshots, dimensionId)
        .map(snapshot -> MtrDataMapper.buildDepotTrains(snapshot, depotId))
        .orElseGet(Collections::emptyList);
  }

  @Override
  public List<MtrDimensionSnapshot> fetchSnapshots() {
    return captureSnapshots();
  }

  private List<MtrDimensionSnapshot> captureSnapshots() {
    return snapshotCache.get();
  }

  private List<MtrDimensionSnapshot> captureSnapshotsNow() {
    MinecraftServer server = serverSupplier.get();
    if (server == null) {
      return Collections.emptyList();
    }
    List<MtrDimensionSnapshot> snapshots = new ArrayList<>();
    try {
      for (ServerLevel level : server.getAllLevels()) {
        try {
          final RailwayData data = MtrRailwayDataAccess.resolve(level);
          if (data != null) {
            snapshots.add(new MtrDimensionSnapshot(resolveDimensionId(level), data));
          }
        } catch (Throwable throwable) {
          ResourceLocation id = level.dimension().location();
          LOGGER.debug("Failed to sample MTR data for {}", id, throwable);
        }
      }
    } catch (Throwable throwable) {
      LOGGER.warn("Unable to enumerate Forge server levels", throwable);
      return Collections.emptyList();
    }
    return snapshots;
  }

  private static String resolveDimensionId(ServerLevel level) {
    return level.dimension().location().toString();
  }

  private Optional<MtrDimensionSnapshot> findSnapshot(
      List<MtrDimensionSnapshot> snapshots, String dimensionId) {
    if (snapshots.isEmpty() || dimensionId == null || dimensionId.isEmpty()) {
      return Optional.empty();
    }
    return snapshots.stream()
        .filter(snapshot -> snapshot.getDimensionId().equals(dimensionId))
        .findFirst();
  }
}
