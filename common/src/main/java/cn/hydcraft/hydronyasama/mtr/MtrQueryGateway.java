package cn.hydcraft.hydronyasama.mtr;

import cn.hydcraft.hydronyasama.mtr.MtrModels.DepotInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.DimensionOverview;
import cn.hydcraft.hydronyasama.mtr.MtrModels.FareAreaInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.NodePage;
import cn.hydcraft.hydronyasama.mtr.MtrModels.RouteDetail;
import cn.hydcraft.hydronyasama.mtr.MtrModels.StationInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.StationTimetable;
import cn.hydcraft.hydronyasama.mtr.MtrModels.TrainStatus;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Loader-side service that knows how to fetch MTR data for the current Minecraft server. */
public interface MtrQueryGateway {
  /**
   * @return {@code true} if the loader has registered a working implementation and MTR is loaded.
   */
  default boolean isReady() {
    return true;
  }

  List<DimensionOverview> fetchNetworkOverview();

  Optional<RouteDetail> fetchRouteDetail(String dimensionId, long routeId);

  List<DepotInfo> fetchDepots(String dimensionId);

  List<FareAreaInfo> fetchFareAreas(String dimensionId);

  NodePage fetchNodes(String dimensionId, String cursor, int limit);

  Optional<StationTimetable> fetchStationTimetable(
      String dimensionId, long stationId, Long platformId);

  List<StationInfo> fetchStations(String dimensionId);

  /**
   * Returns the list of trains in the requested dimension/route. If {@code dimensionId} is empty or
   * {@code null}, all loaded dimensions are scanned.
   */
  List<TrainStatus> fetchRouteTrains(String dimensionId, long routeId);

  List<TrainStatus> fetchDepotTrains(String dimensionId, long depotId);

  default List<MtrDimensionSnapshot> fetchSnapshots() {
    return Collections.emptyList();
  }

  MtrQueryGateway UNAVAILABLE =
      new MtrQueryGateway() {
        @Override
        public boolean isReady() {
          return false;
        }

        @Override
        public List<DimensionOverview> fetchNetworkOverview() {
          return Collections.emptyList();
        }

        @Override
        public Optional<RouteDetail> fetchRouteDetail(String dimensionId, long routeId) {
          return Optional.empty();
        }

        @Override
        public List<DepotInfo> fetchDepots(String dimensionId) {
          return Collections.emptyList();
        }

        @Override
        public List<FareAreaInfo> fetchFareAreas(String dimensionId) {
          return Collections.emptyList();
        }

        @Override
        public NodePage fetchNodes(String dimensionId, String cursor, int limit) {
          return new NodePage(
              dimensionId == null ? "" : dimensionId,
              Collections.<MtrModels.NodeInfo>emptyList(),
              null);
        }

        @Override
        public Optional<StationTimetable> fetchStationTimetable(
            String dimensionId, long stationId, Long platformId) {
          return Optional.empty();
        }

        @Override
        public List<StationInfo> fetchStations(String dimensionId) {
          return Collections.emptyList();
        }

        @Override
        public List<TrainStatus> fetchRouteTrains(String dimensionId, long routeId) {
          return Collections.emptyList();
        }

        @Override
        public List<TrainStatus> fetchDepotTrains(String dimensionId, long depotId) {
          return Collections.emptyList();
        }
      };
}
