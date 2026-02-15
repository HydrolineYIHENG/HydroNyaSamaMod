package cn.hydcraft.hydronyasama.service.create;

import cn.hydcraft.hydronyasama.create.CreateJsonWriter;
import cn.hydcraft.hydronyasama.create.CreateQueryGateway;
import cn.hydcraft.hydronyasama.create.CreateRealtimeSnapshot;
import cn.hydcraft.hydronyasama.protocol.BeaconMessage;
import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import cn.hydcraft.hydronyasama.transport.TransportContext;
import com.google.gson.JsonObject;

public final class CreateGetRealtimeActionHandler extends AbstractCreateActionHandler {
  public static final String ACTION = "create:get_realtime";

  @Override
  public String action() {
    return ACTION;
  }

  @Override
  public BeaconResponse handle(BeaconMessage message, TransportContext context) {
    CreateQueryGateway gateway = gateway();
    if (!gateway.isReady()) {
      return notReady(message.getRequestId());
    }
    CreateRealtimeSnapshot snapshot = gateway.fetchRealtimeSnapshot();
    JsonObject responsePayload = CreateJsonWriter.writeRealtimeSnapshot(snapshot);
    return ok(message.getRequestId(), responsePayload);
  }
}
