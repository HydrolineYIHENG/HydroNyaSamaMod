package cn.hydcraft.hydronyasama.mtr;

import cn.hydcraft.hydronyasama.mtr.MtrModels.Bounds;
import cn.hydcraft.hydronyasama.mtr.MtrModels.DepotInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.DimensionOverview;
import cn.hydcraft.hydronyasama.mtr.MtrModels.FareAreaInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.NodeInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.NodePage;
import cn.hydcraft.hydronyasama.mtr.MtrModels.PlatformSummary;
import cn.hydcraft.hydronyasama.mtr.MtrModels.PlatformTimetable;
import cn.hydcraft.hydronyasama.mtr.MtrModels.RouteDetail;
import cn.hydcraft.hydronyasama.mtr.MtrModels.RouteNode;
import cn.hydcraft.hydronyasama.mtr.MtrModels.RouteSummary;
import cn.hydcraft.hydronyasama.mtr.MtrModels.ScheduleEntry;
import cn.hydcraft.hydronyasama.mtr.MtrModels.StationInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.StationPlatformInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.StationTimetable;
import cn.hydcraft.hydronyasama.mtr.MtrModels.TrainStatus;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import mtr.data.AreaBase;
import mtr.data.DataCache;
import mtr.data.Depot;
import mtr.data.Platform;
import mtr.data.Rail;
import mtr.data.RailType;
import mtr.data.RailwayData;
import mtr.data.Route;
import mtr.data.Route.RoutePlatform;
import mtr.data.SavedRailBase;
import mtr.data.Siding;
import mtr.data.Station;
import mtr.data.Train;
import mtr.data.TrainServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Helper utilities that convert live MTR data into the DTOs defined under {@link MtrModels}. */
public final class MtrDataMapper {
  private static final Logger LOGGER = LoggerFactory.getLogger(MtrDataMapper.class);
  private static final Method SAVED_RAIL_GET_MID_POS =
      locateMethod(SavedRailBase.class, "getMidPos");
  private static final Method AREA_BASE_GET_CENTER = locateMethod(AreaBase.class, "getCenter");
  private static final Map<Class<?>, Method> AS_LONG_METHOD_CACHE = new ConcurrentHashMap<>();
  private static final Field AREA_CORNER1 = locateField(AreaBase.class, "corner1");
  private static final Field AREA_CORNER2 = locateField(AreaBase.class, "corner2");
  private static final Field DATA_CACHE_BLOCK_POS_TO_STATION =
      locateField(DataCache.class, "blockPosToStation");
  private static final Field SIDING_TRAINS = locateField(Siding.class, "trains");
  private static final Field SIDING_DEPOT = locateField(Siding.class, "depot");
  private static final Field TRAIN_SERVER_ROUTE_ID = locateField(TrainServer.class, "routeId");
  private static final Field TRAIN_NEXT_STOPPING_INDEX =
      locateField(Train.class, "nextStoppingIndex");
  private static final Field TRAIN_PATH_FIELD = locateField(Train.class, "path");
  private static final Class<?> PATH_DATA_CLASS = resolvePathDataClass();
  private static final Field PATH_DATA_SAVED_RAIL_BASE_ID = locatePathDataField("savedRailBaseId");

  private MtrDataMapper() {}

  public static List<DimensionOverview> buildNetworkOverview(List<MtrDimensionSnapshot> snapshots) {
    if (snapshots == null || snapshots.isEmpty()) {
      return Collections.emptyList();
    }
    List<DimensionOverview> results = new ArrayList<>(snapshots.size());
    for (MtrDimensionSnapshot snapshot : snapshots) {
      DimensionContext context = DimensionContext.from(snapshot);
      if (context == null) {
        continue;
      }
      results.add(
          new DimensionOverview(
              context.dimensionId,
              buildRouteSummaries(context),
              buildDepotInfos(context),
              buildFareAreaInfos(context)));
    }
    return results;
  }

  public static Optional<RouteDetail> buildRouteDetail(
      MtrDimensionSnapshot snapshot, long routeId) {
    DimensionContext context = DimensionContext.from(snapshot);
    if (context == null) {
      return Optional.empty();
    }
    return context.routes.stream()
        .filter(route -> route.id == routeId)
        .findFirst()
        .map(
            route ->
                new RouteDetail(
                    context.dimensionId,
                    route.id,
                    safeName(route.name),
                    route.color,
                    describeRouteType(route),
                    buildRouteNodes(context, route)));
  }

  public static List<DepotInfo> buildDepots(MtrDimensionSnapshot snapshot) {
    DimensionContext context = DimensionContext.from(snapshot);
    if (context == null) {
      return Collections.emptyList();
    }
    return buildDepotInfos(context);
  }

  public static List<FareAreaInfo> buildFareAreas(MtrDimensionSnapshot snapshot) {
    DimensionContext context = DimensionContext.from(snapshot);
    if (context == null) {
      return Collections.emptyList();
    }
    return buildFareAreaInfos(context);
  }

  public static List<StationInfo> buildStations(MtrDimensionSnapshot snapshot) {
    DimensionContext context = DimensionContext.from(snapshot);
    if (context == null) {
      return Collections.emptyList();
    }
    List<StationInfo> stations = new ArrayList<>(context.stations.size());
    context.stations.values().stream()
        .sorted(Comparator.comparingLong(station -> station.id))
        .forEach(
            station ->
                stations.add(
                    new StationInfo(
                        context.dimensionId,
                        station.id,
                        safeName(station.name),
                        station.zone,
                        buildBoundsFromArea(station),
                        toSortedList(context.stationRouteIds.get(station.id)),
                        buildStationPlatforms(context, station))));
    return stations;
  }

  public static NodePage buildNodePage(MtrDimensionSnapshot snapshot, String cursor, int limit) {
    DimensionContext context = DimensionContext.from(snapshot);
    if (context == null) {
      return new NodePage(snapshot.getDimensionId(), Collections.emptyList(), null);
    }
    List<NodeInfo> allNodes = collectNodes(context);
    if (allNodes.isEmpty()) {
      return new NodePage(context.dimensionId, Collections.emptyList(), null);
    }
    int safeLimit = Math.max(1, limit);
    int offset = parseCursor(cursor);
    if (offset >= allNodes.size()) {
      return new NodePage(context.dimensionId, Collections.emptyList(), null);
    }
    int end = Math.min(allNodes.size(), offset + safeLimit);
    List<NodeInfo> slice = new ArrayList<>(allNodes.subList(offset, end));
    String nextCursor = end < allNodes.size() ? Integer.toString(end) : null;
    return new NodePage(context.dimensionId, slice, nextCursor);
  }

  public static Optional<StationTimetable> buildStationTimetable(
      MtrDimensionSnapshot snapshot, long stationId, Long platformId) {
    DimensionContext context = DimensionContext.from(snapshot);
    if (context == null || !context.stations.containsKey(stationId)) {
      return Optional.empty();
    }
    Map<Long, List<mtr.data.ScheduleEntry>> scheduleMap = new HashMap<>();
    context.railwayData.getSchedulesForStation(scheduleMap, stationId);
    if (scheduleMap.isEmpty()) {
      return Optional.empty();
    }
    List<PlatformTimetable> platforms =
        scheduleMap.entrySet().stream()
            .filter(entry -> platformId == null || Objects.equals(entry.getKey(), platformId))
            .sorted(Map.Entry.comparingByKey())
            .map(
                entry ->
                    new PlatformTimetable(
                        entry.getKey(), toScheduleEntries(context, entry.getValue())))
            .collect(Collectors.toList());
    if (platforms.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new StationTimetable(context.dimensionId, stationId, platforms));
  }

  public static List<TrainStatus> buildRouteTrains(MtrDimensionSnapshot snapshot, long routeId) {
    DimensionContext context = DimensionContext.from(snapshot);
    if (context == null) {
      return Collections.emptyList();
    }
    List<TrainStatus> trains = collectTrainStatuses(context);
    if (routeId == 0 || trains.isEmpty()) {
      return trains;
    }
    return trains.stream()
        .filter(status -> status.getRouteId() == routeId)
        .collect(Collectors.toList());
  }

  public static List<TrainStatus> buildDepotTrains(MtrDimensionSnapshot snapshot, long depotId) {
    DimensionContext context = DimensionContext.from(snapshot);
    if (context == null) {
      return Collections.emptyList();
    }
    List<TrainStatus> trains = collectTrainStatuses(context);
    if (depotId == 0 || trains.isEmpty()) {
      return trains;
    }
    return trains.stream()
        .filter(status -> status.getDepotId().map(id -> id == depotId).orElse(false))
        .collect(Collectors.toList());
  }

  private static List<RouteSummary> buildRouteSummaries(DimensionContext context) {
    if (context.routes.isEmpty()) {
      return Collections.emptyList();
    }
    List<RouteSummary> summaries = new ArrayList<>(context.routes.size());
    context.routes.stream()
        .sorted(Comparator.comparingLong(route -> route.id))
        .forEach(
            route ->
                summaries.add(
                    new RouteSummary(
                        route.id,
                        safeName(route.name),
                        route.color,
                        describeTransportMode(route.transportMode),
                        describeRouteType(route),
                        route.isHidden,
                        buildPlatformSummaries(context, route))));
    return summaries;
  }

  private static List<DepotInfo> buildDepotInfos(DimensionContext context) {
    if (context.depots.isEmpty()) {
      return Collections.emptyList();
    }
    List<DepotInfo> depots = new ArrayList<>(context.depots.size());
    context.depots.stream()
        .sorted(Comparator.comparingLong(depot -> depot.id))
        .forEach(
            depot ->
                depots.add(
                    new DepotInfo(
                        depot.id,
                        safeName(depot.name),
                        describeTransportMode(depot.transportMode),
                        copyLongList(depot.routeIds),
                        copyIntegerList(depot.departures),
                        depot.useRealTime,
                        depot.repeatInfinitely,
                        depot.cruisingAltitude,
                        depot.getNextDepartureMillis())));
    return depots;
  }

  private static List<FareAreaInfo> buildFareAreaInfos(DimensionContext context) {
    if (context.stations.isEmpty()) {
      return Collections.emptyList();
    }
    List<FareAreaInfo> results = new ArrayList<>(context.stations.size());
    context.stations.values().stream()
        .sorted(Comparator.comparingLong(station -> station.id))
        .forEach(
            station ->
                results.add(
                    new FareAreaInfo(
                        station.id,
                        safeName(station.name),
                        station.zone,
                        buildBoundsFromArea(station),
                        toSortedList(context.stationRouteIds.get(station.id)))));
    return results;
  }

  private static List<RouteNode> buildRouteNodes(DimensionContext context, Route route) {
    if (route.platformIds == null || route.platformIds.isEmpty()) {
      return buildStationRouteNodes(context, route);
    }
    List<RouteNode> nodes = new ArrayList<>(route.platformIds.size());
    long sequence = 0;
    for (Route.RoutePlatform reference : route.platformIds) {
      Platform platform = context.platforms.get(reference.platformId);
      if (platform == null) {
        continue;
      }
      Station station = context.platformToStation.get(reference.platformId);
      NodeInfo nodeInfo =
          buildNodeInfoFromSavedRail(platform, station != null ? station.id : null, true);
      if (nodeInfo != null) {
        nodes.add(new RouteNode(nodeInfo, "PLATFORM", sequence++));
      }
    }
    List<Long> stationOrder = context.routeStationOrder.get(route.id);
    if ((stationOrder != null && stationOrder.size() > nodes.size()) || nodes.size() <= 2) {
      List<RouteNode> fallback = buildStationRouteNodes(context, route);
      if (!fallback.isEmpty()) {
        return fallback;
      }
    }
    return nodes.isEmpty() ? buildStationRouteNodes(context, route) : nodes;
  }

  private static List<RouteNode> buildStationRouteNodes(DimensionContext context, Route route) {
    List<Long> stationOrder = context.routeStationOrder.get(route.id);
    if (stationOrder == null || stationOrder.isEmpty()) {
      return Collections.emptyList();
    }
    List<RouteNode> nodes = new ArrayList<>(stationOrder.size());
    long sequence = 0;
    for (Long stationId : stationOrder) {
      Station station = context.stations.get(stationId);
      if (station == null) {
        continue;
      }
      NodeInfo nodeInfo = buildNodeInfoFromStation(station);
      if (nodeInfo != null) {
        nodes.add(new RouteNode(nodeInfo, "STATION", sequence++));
      }
    }
    return nodes;
  }

  private static List<PlatformSummary> buildPlatformSummaries(
      DimensionContext context, Route route) {
    if (route.platformIds == null || route.platformIds.isEmpty()) {
      return Collections.emptyList();
    }
    List<PlatformSummary> summaries = new ArrayList<>(route.platformIds.size());
    for (Route.RoutePlatform reference : route.platformIds) {
      Platform platform = context.platforms.get(reference.platformId);
      if (platform == null) {
        continue;
      }
      Station station = context.platformToStation.get(reference.platformId);
      Bounds bounds = buildBoundsFromSavedRail(platform);
      List<Long> interchangeRoutes =
          station != null
              ? toSortedList(context.stationRouteIds.get(station.id))
              : Collections.emptyList();
      summaries.add(
          new PlatformSummary(
              platform.id,
              station != null ? station.id : 0L,
              station != null ? safeName(station.name) : "",
              bounds,
              interchangeRoutes));
    }
    return summaries;
  }

  private static List<TrainStatus> collectTrainStatuses(DimensionContext context) {
    if (context.sidings.isEmpty()) {
      return Collections.emptyList();
    }
    List<TrainStatus> statuses = new ArrayList<>();
    for (Siding siding : context.sidings) {
      Collection<TrainServer> sidingTrains = readTrainServers(siding);
      if (sidingTrains.isEmpty()) {
        continue;
      }
      Long depotId = resolveDepotId(siding);
      for (TrainServer train : sidingTrains) {
        TrainStatus status = toTrainStatus(context, train, depotId);
        if (status != null) {
          statuses.add(status);
        }
      }
    }
    return statuses;
  }

  @SuppressWarnings("unchecked")
  private static Collection<TrainServer> readTrainServers(Siding siding) {
    Object value = readField(SIDING_TRAINS, siding);
    if (value instanceof Collection<?>) {
      Collection<?> collection = (Collection<?>) value;
      if (collection.isEmpty()) {
        return Collections.emptyList();
      }
      List<TrainServer> trains = new ArrayList<>(collection.size());
      for (Object element : collection) {
        if (element instanceof TrainServer) {
          trains.add((TrainServer) element);
        }
      }
      return trains;
    }
    return Collections.emptyList();
  }

  private static Long resolveDepotId(Siding siding) {
    Object value = readField(SIDING_DEPOT, siding);
    if (value instanceof Depot) {
      return ((Depot) value).id;
    }
    return null;
  }

  private static TrainStatus toTrainStatus(
      DimensionContext context, TrainServer train, Long depotId) {
    long routeId = readLong(TRAIN_SERVER_ROUTE_ID, train);
    if (routeId == 0) {
      return null;
    }
    List<Long> stationOrder = context.routeStationOrder.get(routeId);
    int nextStopIndex = readInt(TRAIN_NEXT_STOPPING_INDEX, train, -1);
    Long nextStationId = resolveStationFromIndex(stationOrder, nextStopIndex);
    Long currentStationId = resolveStationFromIndex(stationOrder, nextStopIndex - 1);
    String segmentCategory = train.getIsOnRoute() ? "ROUTE" : "DEPOT";
    double progress = normalizeRailProgress(train);
    UUID uuid = resolveTrainUuid(context.dimensionId, train);
    Optional<Long> railId = resolveRailSegmentId(train);
    return new TrainStatus(
        context.dimensionId,
        uuid,
        train.trainId,
        routeId,
        depotId,
        describeTransportMode(train.transportMode),
        currentStationId,
        nextStationId,
        null,
        segmentCategory,
        progress,
        railId.orElse(null),
        null);
  }

  private static Long resolveStationFromIndex(List<Long> stations, int index) {
    if (stations == null || stations.isEmpty() || index < 0 || index >= stations.size()) {
      return null;
    }
    return stations.get(index);
  }

  private static double normalizeRailProgress(Train train) {
    double progress = train.getRailProgress();
    List<?> path = train.path;
    if (path != null && !path.isEmpty()) {
      double normalized = progress / path.size();
      return Math.max(0D, Math.min(1D, normalized));
    }
    return 0D;
  }

  private static UUID resolveTrainUuid(String dimensionId, Train train) {
    String key =
        train.trainId != null && !train.trainId.isEmpty() ? train.trainId : Long.toString(train.id);
    String combined = dimensionId + ":" + key;
    return UUID.nameUUIDFromBytes(combined.getBytes(StandardCharsets.UTF_8));
  }

  private static List<StationPlatformInfo> buildStationPlatforms(
      DimensionContext context, Station station) {
    if (context.platforms.isEmpty()) {
      return Collections.emptyList();
    }
    List<StationPlatformInfo> platforms = new ArrayList<>();
    context.platforms.values().stream()
        .filter(
            platform -> {
              Station mapped = context.platformToStation.get(platform.id);
              return mapped != null && mapped.id == station.id;
            })
        .sorted(Comparator.comparingLong(platform -> platform.id))
        .forEach(
            platform ->
                platforms.add(
                    new StationPlatformInfo(
                        platform.id,
                        safeName(platform.name),
                        toSortedList(context.platformRouteIds.get(platform.id)),
                        context.platformDepotIds.get(platform.id))));
    return platforms;
  }

  private static List<NodeInfo> collectNodes(DimensionContext context) {
    Map<Object, Map<Object, Rail>> rails = RailsFieldAccessor.get(context.railwayData);
    if (rails.isEmpty()) {
      return Collections.emptyList();
    }
    Map<Long, NodeAccumulator> accumulators = new HashMap<>();
    rails.forEach(
        (startPos, edges) -> {
          NodeAccumulator start = NodeAccumulator.obtain(accumulators, startPos, context);
          if (edges != null) {
            edges.forEach(
                (endPos, rail) -> {
                  if (start != null) {
                    start.absorb(rail);
                  }
                  NodeAccumulator end = NodeAccumulator.obtain(accumulators, endPos, context);
                  if (end != null) {
                    end.absorb(rail);
                  }
                });
          }
        });
    if (accumulators.isEmpty()) {
      return Collections.emptyList();
    }
    List<Map.Entry<Long, NodeAccumulator>> entries = new ArrayList<>(accumulators.entrySet());
    entries.sort(Map.Entry.comparingByKey());
    List<NodeInfo> nodes = new ArrayList<>(entries.size());
    for (Map.Entry<Long, NodeAccumulator> entry : entries) {
      NodeInfo info = entry.getValue().toNodeInfo();
      if (info != null) {
        nodes.add(info);
      }
    }
    return nodes;
  }

  private static List<ScheduleEntry> toScheduleEntries(
      DimensionContext context, List<mtr.data.ScheduleEntry> entries) {
    if (entries == null || entries.isEmpty()) {
      return Collections.emptyList();
    }
    return entries.stream()
        .sorted(Comparator.comparingLong(entry -> entry.arrivalMillis))
        .map(
            entry -> {
              Route route = context.routeIdMap.get(entry.routeId);
              String routeName = route != null ? nullIfEmpty(safeName(route.name)) : null;
              String destination =
                  route != null
                      ? nullIfEmpty(route.getDestination(entry.currentStationIndex))
                      : null;
              String routeLabel =
                  route != null && route.isLightRailRoute
                      ? nullIfEmpty(route.lightRailRouteNumber)
                      : null;
              String circular = route != null ? describeCircularState(route.circularState) : null;
              Integer routeColor = route != null ? route.color : null;
              return new ScheduleEntry(
                  entry.routeId,
                  entry.arrivalMillis,
                  entry.trainCars,
                  entry.currentStationIndex,
                  null,
                  routeName,
                  destination,
                  routeLabel,
                  circular,
                  routeColor);
            })
        .collect(Collectors.toList());
  }

  private static Bounds buildBoundsFromArea(AreaBase area) {
    Object corner1 = readField(AREA_CORNER1, area);
    Object corner2 = readField(AREA_CORNER2, area);
    if (area == null || corner1 == null || corner2 == null) {
      return null;
    }
    TupleValues min = TupleValues.from(corner1);
    TupleValues max = TupleValues.from(corner2);
    if (min == null || max == null) {
      return null;
    }
    int y = resolveAreaY(area);
    int minX = Math.min(min.x, max.x);
    int maxX = Math.max(min.x, max.x);
    int minZ = Math.min(min.z, max.z);
    int maxZ = Math.max(min.z, max.z);
    return new Bounds(minX, y, minZ, maxX, y, maxZ);
  }

  private static Bounds buildBoundsFromSavedRail(SavedRailBase rail) {
    if (rail == null) {
      return null;
    }
    NodeInfo nodeInfo = buildNodeInfoFromSavedRail(rail, null, false);
    if (nodeInfo == null) {
      return null;
    }
    return new Bounds(
        nodeInfo.getX(),
        nodeInfo.getY(),
        nodeInfo.getZ(),
        nodeInfo.getX(),
        nodeInfo.getY(),
        nodeInfo.getZ());
  }

  private static NodeInfo buildNodeInfoFromStation(Station station) {
    if (station == null) {
      return null;
    }
    Bounds bounds = buildBoundsFromArea(station);
    if (bounds == null) {
      return null;
    }
    int centerX = bounds.getMinX() + (bounds.getMaxX() - bounds.getMinX()) / 2;
    int centerY = bounds.getMinY();
    int centerZ = bounds.getMinZ() + (bounds.getMaxZ() - bounds.getMinZ()) / 2;
    return new NodeInfo(centerX, centerY, centerZ, "STATION", false, station.id);
  }

  private static NodeInfo buildNodeInfoFromSavedRail(
      SavedRailBase rail, Long stationId, boolean platformSegment) {
    if (rail == null || SAVED_RAIL_GET_MID_POS == null) {
      return null;
    }
    Object blockPos = invoke(SAVED_RAIL_GET_MID_POS, rail);
    BlockPosCoord coord = BlockPosEncoding.coordinates(blockPos);
    if (coord == null) {
      return null;
    }
    String railType = platformSegment ? RailType.PLATFORM.name() : "UNKNOWN";
    return new NodeInfo(coord.x, coord.y, coord.z, railType, platformSegment, stationId);
  }

  private static int resolveAreaY(AreaBase area) {
    if (AREA_BASE_GET_CENTER == null) {
      return 0;
    }
    Object blockPos = invoke(AREA_BASE_GET_CENTER, area);
    BlockPosCoord coord = BlockPosEncoding.coordinates(blockPos);
    return coord != null ? coord.y : 0;
  }

  private static List<Long> toSortedList(Set<Long> values) {
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    }
    List<Long> list = new ArrayList<>(values);
    list.sort(Long::compareTo);
    return list;
  }

  private static List<Long> copyLongList(List<Long> values) {
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    }
    return new ArrayList<>(values);
  }

  private static List<Integer> copyIntegerList(List<Integer> values) {
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    }
    return new ArrayList<>(values);
  }

  private static String safeName(String name) {
    return name == null ? "" : name;
  }

  private static String nullIfEmpty(String value) {
    return value == null || value.isEmpty() ? null : value;
  }

  private static String describeCircularState(Route.CircularState state) {
    if (state == null) {
      return null;
    }
    String name = state.toString();
    if ("CLOCKWISE".equals(name)) {
      return "cw";
    }
    if ("COUNTERCLOCKWISE".equals(name)) {
      return "ccw";
    }
    return null;
  }

  private static String describeTransportMode(mtr.data.TransportMode mode) {
    return mode == null ? "UNKNOWN" : mode.name();
  }

  private static String describeRouteType(Route route) {
    if (route.routeType != null) {
      return route.routeType.name();
    }
    return route.isLightRailRoute ? "LIGHT_RAIL" : "NORMAL";
  }

  private static int parseCursor(String cursor) {
    if (cursor == null || cursor.isEmpty()) {
      return 0;
    }
    try {
      return Math.max(0, Integer.parseInt(cursor));
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private static Method locateMethod(Class<?> owner, String name) {
    try {
      Method method = owner.getMethod(name);
      method.setAccessible(true);
      return method;
    } catch (NoSuchMethodException ex) {
      LOGGER.warn("Unable to find method {} on {}", name, owner.getName());
      return null;
    }
  }

  private static Field locateField(Class<?> owner, String name) {
    try {
      Field field = owner.getDeclaredField(name);
      field.setAccessible(true);
      return field;
    } catch (NoSuchFieldException ex) {
      LOGGER.warn("Unable to find field {} on {}", name, owner.getName());
      return null;
    }
  }

  private static Class<?> resolvePathDataClass() {
    try {
      return Class.forName("mtr.path.PathData");
    } catch (ClassNotFoundException ex) {
      LOGGER.debug("PathData class not available", ex);
      return null;
    }
  }

  private static Field locatePathDataField(String name) {
    Class<?> clazz = PATH_DATA_CLASS;
    if (clazz == null) {
      return null;
    }
    return locateField(clazz, name);
  }

  private static Object invoke(Method method, Object target) {
    if (method == null || target == null) {
      return null;
    }
    try {
      return method.invoke(target);
    } catch (IllegalAccessException | InvocationTargetException ex) {
      LOGGER.debug("Failed to invoke {} on {}", method.getName(), target.getClass().getName(), ex);
      return null;
    }
  }

  private static Object readField(Field field, Object target) {
    if (field == null || target == null) {
      return null;
    }
    try {
      return field.get(target);
    } catch (IllegalAccessException ex) {
      LOGGER.debug(
          "Failed to read field {} on {}", field.getName(), target.getClass().getName(), ex);
      return null;
    }
  }

  private static Optional<Long> resolveRailSegmentId(Train train) {
    if (train == null || TRAIN_PATH_FIELD == null || PATH_DATA_SAVED_RAIL_BASE_ID == null) {
      return Optional.empty();
    }
    Object pathValue = readField(TRAIN_PATH_FIELD, train);
    if (!(pathValue instanceof List<?>)) {
      return Optional.empty();
    }
    List<?> pathList = (List<?>) pathValue;
    if (pathList.isEmpty()) {
      return Optional.empty();
    }
    double progress = train.getRailProgress();
    int index;
    try {
      index = train.getIndex(progress, true);
    } catch (Throwable ex) {
      LOGGER.debug("Unable to compute train path index", ex);
      return Optional.empty();
    }
    if (index < 0) {
      index = 0;
    }
    if (index >= pathList.size()) {
      index = pathList.size() - 1;
    }
    Object pathData = pathList.get(index);
    if (pathData == null) {
      return Optional.empty();
    }
    Object rawValue = readField(PATH_DATA_SAVED_RAIL_BASE_ID, pathData);
    if (rawValue instanceof Number) {
      long value = ((Number) rawValue).longValue();
      if (value != 0L) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }

  private static long readLong(Field field, Object target) {
    Object value = readField(field, target);
    if (value instanceof Number) {
      return ((Number) value).longValue();
    }
    return 0L;
  }

  private static int readInt(Field field, Object target, int fallback) {
    Object value = readField(field, target);
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    return fallback;
  }

  @SuppressWarnings("unchecked")
  private static Map<Object, Station> extractBlockPosStationMap(DataCache cache) {
    Object value = readField(DATA_CACHE_BLOCK_POS_TO_STATION, cache);
    if (value instanceof Map) {
      return (Map<Object, Station>) value;
    }
    return Collections.emptyMap();
  }

  private static final class DimensionContext {
    final String dimensionId;
    final RailwayData railwayData;
    final Map<Long, Station> stations;
    final Map<Long, Platform> platforms;
    final Map<Long, Station> platformToStation;
    final Map<Object, Station> blockPosToStation;
    final List<Route> routes;
    final List<Depot> depots;
    final List<Siding> sidings;
    final Map<Long, Set<Long>> stationRouteIds;
    final Map<Long, Set<Long>> platformRouteIds;
    final Map<Long, Long> platformDepotIds;
    final Map<Long, List<Long>> routeStationOrder;
    final Map<Long, Route> routeIdMap;

    private DimensionContext(String dimensionId, RailwayData railwayData, DataCache cache) {
      this.dimensionId = dimensionId;
      this.railwayData = railwayData;
      this.stations = cache.stationIdMap != null ? cache.stationIdMap : Collections.emptyMap();
      this.platforms = cache.platformIdMap != null ? cache.platformIdMap : Collections.emptyMap();
      this.platformToStation =
          cache.platformIdToStation != null ? cache.platformIdToStation : Collections.emptyMap();
      this.blockPosToStation = extractBlockPosStationMap(cache);
      this.routes =
          railwayData.routes != null
              ? new ArrayList<>(railwayData.routes)
              : Collections.emptyList();
      this.depots =
          railwayData.depots != null
              ? new ArrayList<>(railwayData.depots)
              : Collections.emptyList();
      this.sidings =
          railwayData.sidings != null
              ? new ArrayList<>(railwayData.sidings)
              : Collections.emptyList();
      this.stationRouteIds = buildStationRouteIndex(routes, platformToStation);
      this.platformRouteIds = buildPlatformRouteIndex(routes);
      this.platformDepotIds = buildPlatformDepotIndex(depots);
      this.routeStationOrder = buildRouteStationOrder(routes, platformToStation);
      this.routeIdMap = buildRouteIdMap(routes);
    }

    static DimensionContext from(MtrDimensionSnapshot snapshot) {
      if (snapshot == null) {
        return null;
      }
      try {
        DataCache cache = snapshot.refreshAndGetCache();
        if (cache == null) {
          return null;
        }
        return new DimensionContext(snapshot.getDimensionId(), snapshot.getRailwayData(), cache);
      } catch (Exception ex) {
        LOGGER.warn("Failed to refresh MTR cache for dimension {}", snapshot.getDimensionId(), ex);
        return null;
      }
    }
  }

  private static Map<Long, Set<Long>> buildStationRouteIndex(
      List<Route> routes, Map<Long, Station> platformToStation) {
    if (routes.isEmpty() || platformToStation.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Long, Set<Long>> index = new HashMap<>();
    for (Route route : routes) {
      if (route.platformIds == null) {
        continue;
      }
      for (Route.RoutePlatform reference : route.platformIds) {
        Station station = platformToStation.get(reference.platformId);
        if (station == null) {
          continue;
        }
        index.computeIfAbsent(station.id, key -> new LinkedHashSet<>()).add(route.id);
      }
    }
    return index;
  }

  private static Map<Long, Set<Long>> buildPlatformRouteIndex(List<Route> routes) {
    if (routes.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Long, Set<Long>> index = new HashMap<>();
    for (Route route : routes) {
      if (route.platformIds == null) {
        continue;
      }
      for (Route.RoutePlatform reference : route.platformIds) {
        index.computeIfAbsent(reference.platformId, key -> new LinkedHashSet<>()).add(route.id);
      }
    }
    return index;
  }

  private static Map<Long, Long> buildPlatformDepotIndex(List<Depot> depots) {
    if (depots.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Long, Long> index = new HashMap<>();
    for (Depot depot : depots) {
      if (depot.platformTimes == null || depot.platformTimes.isEmpty()) {
        continue;
      }
      for (Map<Long, Float> platformTimes : new ArrayList<>(depot.platformTimes.values())) {
        if (platformTimes == null) {
          continue;
        }
        for (Long platformId : new ArrayList<>(platformTimes.keySet())) {
          index.putIfAbsent(platformId, depot.id);
        }
      }
    }
    return index;
  }

  private static Map<Long, List<Long>> buildRouteStationOrder(
      List<Route> routes, Map<Long, Station> platformToStation) {
    if (routes.isEmpty() || platformToStation.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Long, List<Long>> order = new HashMap<>();
    for (Route route : routes) {
      if (route.platformIds == null || route.platformIds.isEmpty()) {
        continue;
      }
      List<Long> stations = new ArrayList<>();
      for (RoutePlatform reference : route.platformIds) {
        Station station = platformToStation.get(reference.platformId);
        if (station != null) {
          stations.add(station.id);
        }
      }
      order.put(route.id, stations);
    }
    return order;
  }

  private static Map<Long, Route> buildRouteIdMap(List<Route> routes) {
    if (routes.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Long, Route> map = new HashMap<>();
    for (Route route : routes) {
      if (route != null) {
        map.put(route.id, route);
      }
    }
    return map;
  }

  private static final class NodeAccumulator {
    final int x;
    final int y;
    final int z;
    final Long stationId;
    String railType;
    boolean platformSegment;
    int priority;

    private NodeAccumulator(int x, int y, int z, Long stationId) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.stationId = stationId;
    }

    static NodeAccumulator obtain(
        Map<Long, NodeAccumulator> cache, Object blockPos, DimensionContext context) {
      BlockPosCoord coord = BlockPosEncoding.coordinates(blockPos);
      if (coord == null) {
        return null;
      }
      NodeAccumulator existing = cache.get(coord.packed);
      if (existing != null) {
        return existing;
      }
      Station station = context.blockPosToStation.get(blockPos);
      NodeAccumulator created =
          new NodeAccumulator(coord.x, coord.y, coord.z, station != null ? station.id : null);
      cache.put(coord.packed, created);
      return created;
    }

    void absorb(Rail rail) {
      if (rail == null || rail.railType == null) {
        return;
      }
      int candidatePriority = priority(rail.railType);
      if (candidatePriority >= this.priority) {
        this.priority = candidatePriority;
        this.railType = rail.railType.name();
        this.platformSegment = rail.railType == RailType.PLATFORM;
      }
    }

    NodeInfo toNodeInfo() {
      String type = railType != null ? railType : "UNKNOWN";
      return new NodeInfo(x, y, z, type, platformSegment, stationId);
    }

    private static int priority(RailType type) {
      if (type == null) {
        return 0;
      }
      if (type == RailType.PLATFORM) {
        return 3;
      }
      if (type == RailType.SIDING) {
        return 2;
      }
      return 1;
    }
  }

  private static final class TupleValues {
    final int x;
    final int z;

    private TupleValues(int x, int z) {
      this.x = x;
      this.z = z;
    }

    static TupleValues from(Object tuple) {
      if (tuple == null) {
        return null;
      }
      TupleAccessor accessor = TupleAccessor.forClass(tuple.getClass());
      if (accessor == null) {
        return null;
      }
      Integer first = accessor.first(tuple);
      Integer second = accessor.second(tuple);
      if (first == null || second == null) {
        return null;
      }
      return new TupleValues(first, second);
    }
  }

  private static final class TupleAccessor {
    private static final Map<Class<?>, TupleAccessor> CACHE = new ConcurrentHashMap<>();
    private final Field first;
    private final Field second;

    private TupleAccessor(Field first, Field second) {
      this.first = first;
      this.second = second;
    }

    static TupleAccessor forClass(Class<?> clazz) {
      return CACHE.computeIfAbsent(clazz, TupleAccessor::create);
    }

    static TupleAccessor create(Class<?> clazz) {
      Field[] fields = clazz.getDeclaredFields();
      List<Field> valueFields = new ArrayList<>();
      for (Field field : fields) {
        if (!Modifier.isStatic(field.getModifiers())) {
          field.setAccessible(true);
          valueFields.add(field);
        }
      }
      if (valueFields.size() < 2) {
        LOGGER.debug("Tuple class {} does not expose two value fields", clazz.getName());
        return null;
      }
      return new TupleAccessor(valueFields.get(0), valueFields.get(1));
    }

    Integer first(Object target) {
      return read(first, target);
    }

    Integer second(Object target) {
      return read(second, target);
    }

    private Integer read(Field field, Object target) {
      if (field == null || target == null) {
        return null;
      }
      try {
        Object value = field.get(target);
        if (value instanceof Integer) {
          return (Integer) value;
        }
      } catch (IllegalAccessException ex) {
        LOGGER.debug("Unable to read tuple field {}", field.getName(), ex);
      }
      return null;
    }
  }

  private static final class BlockPosCoord {
    final long packed;
    final int x;
    final int y;
    final int z;

    private BlockPosCoord(long packed, int x, int y, int z) {
      this.packed = packed;
      this.x = x;
      this.y = y;
      this.z = z;
    }
  }

  private static final class BlockPosEncoding {
    private static final int X_BITS = 26;
    private static final int Y_BITS = 12;
    private static final int Z_BITS = 26;
    private static final long X_MASK = (1L << X_BITS) - 1L;
    private static final long Y_MASK = (1L << Y_BITS) - 1L;
    private static final long Z_MASK = (1L << Z_BITS) - 1L;
    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET = Y_BITS;
    private static final int X_OFFSET = Y_BITS + Z_BITS;

    private BlockPosEncoding() {}

    static BlockPosCoord coordinates(Object blockPos) {
      if (blockPos == null) {
        return null;
      }
      try {
        long packed = encode(blockPos);
        int x = unpack(packed, X_OFFSET, X_MASK, X_BITS);
        int y = unpack(packed, Y_OFFSET, Y_MASK, Y_BITS);
        int z = unpack(packed, Z_OFFSET, Z_MASK, Z_BITS);
        return new BlockPosCoord(packed, x, y, z);
      } catch (Exception ex) {
        LOGGER.debug("Unable to decode BlockPos {}", blockPos.getClass().getName(), ex);
        return null;
      }
    }

    private static long encode(Object blockPos)
        throws InvocationTargetException, IllegalAccessException {
      Method method =
          AS_LONG_METHOD_CACHE.computeIfAbsent(
              blockPos.getClass(), BlockPosEncoding::locateAsLongMethod);
      if (method == null) {
        throw new IllegalStateException(
            "Missing asLong method for " + blockPos.getClass().getName());
      }
      Object value = method.invoke(blockPos);
      return (long) value;
    }

    private static Method locateAsLongMethod(Class<?> clazz) {
      try {
        Method method = clazz.getMethod("asLong");
        method.setAccessible(true);
        return method;
      } catch (NoSuchMethodException ignored) {
        for (Method candidate : clazz.getMethods()) {
          if (candidate.getParameterCount() == 0 && candidate.getReturnType() == long.class) {
            candidate.setAccessible(true);
            return candidate;
          }
        }
        LOGGER.warn("Unable to locate asLong-like method on {}", clazz.getName());
        return null;
      }
    }

    private static int unpack(long value, int offset, long mask, int bits) {
      long shifted = (value >> offset) & mask;
      int limit = 1 << (bits - 1);
      if (shifted >= limit) {
        shifted -= (1L << bits);
      }
      return (int) shifted;
    }
  }

  private static final class RailsFieldAccessor {
    private static final Field FIELD = locate();

    private RailsFieldAccessor() {}

    @SuppressWarnings("unchecked")
    static Map<Object, Map<Object, Rail>> get(RailwayData data) {
      if (FIELD == null || data == null) {
        return Collections.emptyMap();
      }
      try {
        Object value = FIELD.get(data);
        if (value instanceof Map) {
          return (Map<Object, Map<Object, Rail>>) value;
        }
      } catch (IllegalAccessException ex) {
        LOGGER.debug("Unable to read RailwayData.rails", ex);
      }
      return Collections.emptyMap();
    }

    private static Field locate() {
      try {
        Field field = RailwayData.class.getDeclaredField("rails");
        field.setAccessible(true);
        return field;
      } catch (NoSuchFieldException ex) {
        LOGGER.warn("Failed to locate RailwayData.rails field", ex);
        return null;
      }
    }
  }
}
