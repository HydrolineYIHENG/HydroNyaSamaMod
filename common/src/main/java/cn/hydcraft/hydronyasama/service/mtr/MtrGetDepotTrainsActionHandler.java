package cn.hydcraft.hydronyasama.service.mtr;

import cn.hydcraft.hydronyasama.mtr.MtrJsonWriter;
import cn.hydcraft.hydronyasama.mtr.MtrModels.DepotInfo;
import cn.hydcraft.hydronyasama.mtr.MtrModels.TrainStatus;
import cn.hydcraft.hydronyasama.mtr.MtrQueryGateway;
import cn.hydcraft.hydronyasama.protocol.BeaconMessage;
import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import cn.hydcraft.hydronyasama.transport.TransportContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;

public final class MtrGetDepotTrainsActionHandler extends AbstractMtrActionHandler {
  public static final String ACTION = "mtr:get_depot_trains";

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
    String dimension =
        payload != null && payload.has("dimension") ? payload.get("dimension").getAsString() : null;
    long depotId =
        payload != null && payload.has("depotId") ? payload.get("depotId").getAsLong() : 0L;
    List<DepotInfo> depots = gateway.fetchDepots(dimension);
    if (depots.isEmpty()) {
      return invalidPayload(message.getRequestId(), "no depot data available");
    }
    JsonArray depotArray = new JsonArray();
    for (DepotInfo depot : depots) {
      if (depot == null) {
        continue;
      }
      if (depotId > 0 && depot.getDepotId() != depotId) {
        continue;
      }
      JsonObject depotJson = new JsonObject();
      depotJson.addProperty("depotId", depot.getDepotId());
      depotJson.addProperty("name", depot.getName());
      depotJson.add("departures", writeIntArray(depot.getDepartures()));
      depotJson.add("routeIds", writeLongArray(depot.getRouteIds()));
      depotJson.addProperty("useRealTime", depot.isUseRealTime());
      depotJson.addProperty("repeatInfinitely", depot.isRepeatInfinitely());
      depotJson.addProperty("cruisingAltitude", depot.getCruisingAltitude());
      depot
          .getNextDepartureMillis()
          .ifPresent(value -> depotJson.addProperty("nextDepartureMillis", value));
      List<TrainStatus> trains = gateway.fetchDepotTrains(dimension, depot.getDepotId());
      depotJson.add("trains", MtrJsonWriter.writeTrainStatuses(trains));
      depotArray.add(depotJson);
    }
    if (depotArray.size() == 0) {
      return invalidPayload(message.getRequestId(), "no matching depot");
    }
    JsonObject responsePayload = new JsonObject();
    responsePayload.addProperty("timestamp", System.currentTimeMillis());
    if (dimension != null && !dimension.isEmpty()) {
      responsePayload.addProperty("dimension", dimension);
    }
    responsePayload.add("depots", depotArray);
    return ok(message.getRequestId(), responsePayload);
  }

  private static JsonArray writeLongArray(List<Long> values) {
    JsonArray array = new JsonArray();
    if (values != null) {
      for (Long value : values) {
        if (value != null) {
          array.add(value);
        }
      }
    }
    return array;
  }

  private static JsonArray writeIntArray(List<Integer> values) {
    JsonArray array = new JsonArray();
    if (values != null) {
      for (Integer value : values) {
        if (value != null) {
          array.add(value);
        }
      }
    }
    return array;
  }
}
