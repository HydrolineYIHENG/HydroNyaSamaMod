package cn.hydcraft.hydronyasama.service.mtr;

import cn.hydcraft.hydronyasama.mtr.MtrDimensionSnapshot;
import cn.hydcraft.hydronyasama.mtr.MtrQueryGateway;
import cn.hydcraft.hydronyasama.mtr.RailwayDataSerializer;
import cn.hydcraft.hydronyasama.protocol.BeaconMessage;
import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import cn.hydcraft.hydronyasama.transport.TransportContext;
import cn.hydcraft.hydronyasama.util.PayloadChunker;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Base64;
import java.util.List;

public final class MtrGetRailwaySnapshotActionHandler extends AbstractMtrActionHandler {
  public static final String ACTION = "mtr:get_railway_snapshot";

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
    String requestedDimension =
        payload != null && payload.has("dimension") ? payload.get("dimension").getAsString() : null;
    List<MtrDimensionSnapshot> snapshots = gateway.fetchSnapshots();
    JsonArray serialized = new JsonArray();
    long now = System.currentTimeMillis();
    for (MtrDimensionSnapshot snapshot : snapshots) {
      if (requestedDimension != null && !requestedDimension.equals(snapshot.getDimensionId())) {
        continue;
      }
      byte[] data = RailwayDataSerializer.serialize(snapshot);
      if (data.length == 0) {
        continue;
      }
      JsonObject entry = new JsonObject();
      entry.addProperty("dimension", snapshot.getDimensionId());
      entry.addProperty("format", "messagepack");
      entry.addProperty("timestamp", now);
      entry.addProperty("length", data.length);
      String encoded = Base64.getEncoder().encodeToString(data);
      entry.add(
          "payloadChunks", PayloadChunker.chunkEncodedPayload("base64", encoded, data.length));
      serialized.add(entry);
    }
    if (requestedDimension != null && serialized.size() == 0) {
      return invalidPayload(message.getRequestId(), "unknown dimension");
    }
    JsonObject responsePayload = new JsonObject();
    responsePayload.addProperty("format", "messagepack");
    responsePayload.add("snapshots", serialized);
    return ok(message.getRequestId(), responsePayload);
  }
}
