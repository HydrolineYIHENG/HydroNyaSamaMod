package cn.hydcraft.hydronyasama.mtr;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mtr.data.DataCache;
import mtr.data.Platform;
import mtr.data.RailwayData;
import mtr.data.Route;
import mtr.data.ScheduleEntry;
import mtr.data.Station;

/** Helper that mimics the MTR arrivals servlet to expose station/platform schedules. */
public final class MtrStationScheduleBuilder {
  private MtrStationScheduleBuilder() {}

  public static JsonArray build(
      MtrDimensionSnapshot snapshot,
      long stationId,
      Long platformId,
      Map<Long, String> platformNames) {
    if (snapshot == null) {
      return new JsonArray();
    }
    RailwayData railwayData = snapshot.getRailwayData();
    if (railwayData == null) {
      return new JsonArray();
    }
    DataCache cache = snapshot.refreshAndGetCache();
    if (cache == null) {
      return new JsonArray();
    }
    Map<Long, List<ScheduleEntry>> scheduleMap = new HashMap<>();
    railwayData.getSchedulesForStation(scheduleMap, stationId);
    if (scheduleMap.isEmpty()) {
      return new JsonArray();
    }
    JsonArray platforms = new JsonArray();
    List<Long> platformIds = new ArrayList<>(scheduleMap.keySet());
    Collections.sort(platformIds);
    Map<Long, String> safePlatformNames =
        platformNames == null ? Collections.emptyMap() : platformNames;
    for (Long id : platformIds) {
      if (platformId != null && !platformId.equals(id)) {
        continue;
      }
      List<ScheduleEntry> entries = scheduleMap.get(id);
      if (entries == null || entries.isEmpty()) {
        continue;
      }
      entries.sort(null);
      JsonArray entryArray = new JsonArray();
      for (ScheduleEntry entry : entries) {
        entryArray.add(convert(entry, cache));
      }
      if (entryArray.size() == 0) {
        continue;
      }
      JsonObject platformJson = new JsonObject();
      platformJson.addProperty("platformId", id);
      String platformName = safePlatformNames.get(id);
      if (platformName != null && !platformName.isEmpty()) {
        platformJson.addProperty("platformName", platformName);
      } else {
        Platform platform = cache.platformIdMap.get(id);
        if (platform != null && platform.name != null && !platform.name.isEmpty()) {
          platformJson.addProperty("platformName", platform.name);
        }
      }
      platformJson.add("entries", entryArray);
      platforms.add(platformJson);
    }
    return platforms;
  }

  private static JsonObject convert(ScheduleEntry entry, DataCache cache) {
    JsonObject json = new JsonObject();
    json.addProperty("routeId", entry.routeId);
    Route route = cache.routeIdMap.get(entry.routeId);
    String routeName = route != null ? safeName(route.name) : "";
    if (!routeName.isEmpty()) {
      json.addProperty("routeName", routeName);
      json.addProperty("name", routeName);
    }
    String destination = resolveDestination(route, entry, cache);
    if (destination != null && !destination.isEmpty()) {
      json.addProperty("destination", destination);
    }
    String circular = describeCircularState(route != null ? route.circularState : null);
    if (circular != null) {
      json.addProperty("circular", circular);
    }
    String routeLabel =
        route != null && route.isLightRailRoute && route.lightRailRouteNumber != null
            ? route.lightRailRouteNumber
            : "";
    if (!routeLabel.isEmpty()) {
      json.addProperty("route", routeLabel);
    }
    if (route != null) {
      json.addProperty("color", route.color);
    }
    json.addProperty("arrivalMillis", entry.arrivalMillis);
    json.addProperty("trainCars", entry.trainCars);
    json.addProperty("currentStationIndex", entry.currentStationIndex);
    return json;
  }

  private static String resolveDestination(Route route, ScheduleEntry entry, DataCache cache) {
    if (route == null) {
      return "";
    }
    String destination = route.getDestination(entry.currentStationIndex);
    if (destination != null && !destination.isEmpty()) {
      return destination;
    }
    long lastPlatformId = route.getLastPlatformId();
    if (lastPlatformId == 0L) {
      return "";
    }
    Station station = cache.platformIdToStation.get(lastPlatformId);
    return station != null ? safeName(station.name) : "";
  }

  private static String safeName(String value) {
    return value == null ? "" : value;
  }

  private static String describeCircularState(Route.CircularState state) {
    if (state == null) {
      return "";
    }
    String name = state.name();
    if (name == null || name.isEmpty()) {
      return "";
    }
    String lower = name.toLowerCase(Locale.ROOT);
    if (lower.contains("counter")) {
      return "ccw";
    }
    if (lower.contains("clock")) {
      return "cw";
    }
    return "";
  }
}
