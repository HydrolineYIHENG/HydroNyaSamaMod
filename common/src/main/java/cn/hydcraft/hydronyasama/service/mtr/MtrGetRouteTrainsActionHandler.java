package cn.hydcraft.hydronyasama.service.mtr;

import cn.hydcraft.hydronyasama.mtr.MtrJsonWriter;
import cn.hydcraft.hydronyasama.mtr.MtrModels.TrainStatus;
import cn.hydcraft.hydronyasama.mtr.MtrQueryGateway;
import cn.hydcraft.hydronyasama.protocol.BeaconMessage;
import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import cn.hydcraft.hydronyasama.transport.TransportContext;
import com.google.gson.JsonObject;
import java.util.List;

/** Returns the latest trains running across the requested route/dimension. */
public final class MtrGetRouteTrainsActionHandler extends AbstractMtrActionHandler {
  public static final String ACTION = "mtr:get_route_trains";

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
    long routeId =
        payload != null && payload.has("routeId") ? payload.get("routeId").getAsLong() : 0L;
    List<TrainStatus> statuses = gateway.fetchRouteTrains(dimension, routeId);
    JsonObject responsePayload = new JsonObject();
    responsePayload.addProperty("timestamp", System.currentTimeMillis());
    if (dimension != null && !dimension.isEmpty()) {
      responsePayload.addProperty("dimension", dimension);
    }
    if (payload != null && payload.has("routeId")) {
      responsePayload.addProperty("routeId", routeId);
    }
    responsePayload.add("trains", MtrJsonWriter.writeTrainStatuses(statuses));
    return ok(message.getRequestId(), responsePayload);
  }
}
