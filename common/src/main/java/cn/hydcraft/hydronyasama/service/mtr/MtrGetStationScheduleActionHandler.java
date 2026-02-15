package cn.hydcraft.hydronyasama.service.mtr;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import cn.hydcraft.hydronyasama.mtr.MtrDimensionSnapshot;
import cn.hydcraft.hydronyasama.mtr.MtrModels.DimensionOverview;
import cn.hydcraft.hydronyasama.mtr.MtrModels.StationInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.StationPlatformInfo;
import cn.hydcraft.hydronyasama.mtr.MtrQueryGateway;
import cn.hydcraft.hydronyasama.mtr.MtrStationScheduleBuilder;
import cn.hydcraft.hydronyasama.protocol.BeaconMessage;
import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import cn.hydcraft.hydronyasama.transport.TransportContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public final class MtrGetStationScheduleActionHandler extends AbstractMtrActionHandler {
  public static final String ACTION = "mtr:get_station_schedule";

  @Override
  public String action() {
    return ACTION;
  }

  @Override
  public BeaconResponse handle(BeaconMessage message, TransportContext context) {
    MtrQueryGateway gateway = gateway();
    if (!gateway.isReady()) {
      return notReady(message.getRequestId());
    }
    JsonObject payload = message.getPayload();
    if (payload == null || !payload.has("stationId")) {
      return invalidPayload(message.getRequestId(), "stationId is required");
    }
    long stationId = payload.get("stationId").getAsLong();
    String dimension = payload.has("dimension") ? payload.get("dimension").getAsString() : null;
    Long platformId = payload.has("platformId") ? payload.get("platformId").getAsLong() : null;

    try {
      return MtrScheduleRequestQueue.submit(
          ACTION,
          () ->
              buildStationScheduleResponse(
                  message.getRequestId(), gateway, stationId, dimension, platformId));
    } catch (MtrScheduleRequestQueue.QueueRejectedException e) {
      BeaconProviderMod.LOGGER.warn("Rejecting {} request", ACTION, e);
      return busy(message.getRequestId(), "schedule requests are busy right now");
    } catch (TimeoutException e) {
      BeaconProviderMod.LOGGER.warn("Timeout waiting for {} queue", ACTION, e);
      return busy(message.getRequestId(), "schedule service busy");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      BeaconProviderMod.LOGGER.warn("Interrupted while waiting for {} queue", ACTION, e);
      return busy(message.getRequestId(), "schedule service interrupted");
    } catch (ExecutionException e) {
      BeaconProviderMod.LOGGER.error("Failed to build {} response", ACTION, e.getCause());
      return error(message.getRequestId(), "failed to build station timetable");
    }
  }

  private BeaconResponse buildStationScheduleResponse(
      String requestId,
      MtrQueryGateway gateway,
      long stationId,
      String dimension,
      Long platformId) {
    List<MtrDimensionSnapshot> snapshots = gateway.fetchSnapshots();
    List<DimensionOverview> overviews = gateway.fetchNetworkOverview();
    Set<String> targetDimensions = collectTargetDimensions(dimension, snapshots, overviews);
    if (targetDimensions.isEmpty()) {
      return invalidPayload(requestId, "no registered dimensions");
    }

    Map<String, Map<Long, String>> platformNamesByDimension =
        buildPlatformNameIndex(gateway.fetchStations(null));
    Map<String, MtrDimensionSnapshot> snapshotsByDimension = indexSnapshots(snapshots);

    JsonArray timetablesArray = new JsonArray();
    for (String dimId : targetDimensions) {
      MtrDimensionSnapshot snapshot = snapshotsByDimension.get(dimId);
      if (snapshot == null) {
        continue;
      }
      JsonArray platforms =
          MtrStationScheduleBuilder.build(
              snapshot,
              stationId,
              platformId,
              platformNamesByDimension.getOrDefault(dimId, Collections.emptyMap()));
      if (platforms == null || platforms.size() == 0) {
        continue;
      }
      JsonObject entry = new JsonObject();
      entry.addProperty("dimension", dimId);
      entry.add("platforms", platforms);
      timetablesArray.add(entry);
    }
    if (timetablesArray.size() == 0) {
      return invalidPayload(requestId, "station timetable unavailable");
    }

    JsonObject responsePayload = new JsonObject();
    responsePayload.addProperty("timestamp", System.currentTimeMillis());
    responsePayload.addProperty("stationId", stationId);
    if (dimension != null && !dimension.isEmpty()) {
      responsePayload.addProperty("dimension", dimension);
    }
    responsePayload.add("timetables", timetablesArray);
    return ok(requestId, responsePayload);
  }

  private static Set<String> collectTargetDimensions(
      String requestedDimension,
      List<MtrDimensionSnapshot> snapshots,
      List<DimensionOverview> overviews) {
    Set<String> targets = new LinkedHashSet<>();
    if (requestedDimension != null && !requestedDimension.isEmpty()) {
      targets.add(requestedDimension);
      return targets;
    }
    if (snapshots != null) {
      for (MtrDimensionSnapshot snapshot : snapshots) {
        if (snapshot != null) {
          targets.add(snapshot.getDimensionId());
        }
      }
    }
    if (targets.isEmpty() && overviews != null) {
      for (DimensionOverview overview : overviews) {
        targets.add(overview.getDimensionId());
      }
    }
    return targets;
  }

  private static Map<String, Map<Long, String>> buildPlatformNameIndex(List<StationInfo> stations) {
    Map<String, Map<Long, String>> index = new HashMap<>();
    if (stations == null) {
      return index;
    }
    for (StationInfo station : stations) {
      if (station == null) {
        continue;
      }
      Map<Long, String> names =
          index.computeIfAbsent(station.getDimensionId(), key -> new HashMap<>());
      for (StationPlatformInfo platform : station.getPlatforms()) {
        if (platform == null
            || platform.getPlatformName() == null
            || platform.getPlatformName().isEmpty()) {
          continue;
        }
        names.putIfAbsent(platform.getPlatformId(), platform.getPlatformName());
      }
    }
    return index;
  }

  private static Map<String, MtrDimensionSnapshot> indexSnapshots(
      List<MtrDimensionSnapshot> snapshots) {
    Map<String, MtrDimensionSnapshot> index = new LinkedHashMap<>();
    if (snapshots == null) {
      return index;
    }
    for (MtrDimensionSnapshot snapshot : snapshots) {
      if (snapshot != null) {
        index.putIfAbsent(snapshot.getDimensionId(), snapshot);
      }
    }
    return index;
  }

  // This handler now delegates to {@link MtrStationScheduleBuilder} and no longer builds JSON
  // directly.
}
