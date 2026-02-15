package cn.hydcraft.hydronyasama.service.create;

import cn.hydcraft.hydronyasama.create.CreateQueryGateway;
import cn.hydcraft.hydronyasama.create.CreateQueryRegistry;
import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import cn.hydcraft.hydronyasama.protocol.ResultCode;
import com.google.gson.JsonObject;

abstract class AbstractCreateActionHandler
    implements cn.hydcraft.hydronyasama.service.BeaconActionHandler {
  protected CreateQueryGateway gateway() {
    return CreateQueryRegistry.get();
  }

  protected BeaconResponse notReady(String requestId) {
    return BeaconResponse.builder(requestId)
        .result(ResultCode.NOT_READY)
        .message("Create data not available")
        .build();
  }

  protected BeaconResponse invalidPayload(String requestId, String reason) {
    return BeaconResponse.builder(requestId)
        .result(ResultCode.INVALID_PAYLOAD)
        .message(reason)
        .build();
  }

  protected BeaconResponse ok(String requestId, JsonObject payload) {
    return BeaconResponse.builder(requestId).result(ResultCode.OK).payload(payload).build();
  }

  protected BeaconResponse error(String requestId, String reason) {
    return BeaconResponse.builder(requestId).result(ResultCode.ERROR).message(reason).build();
  }

  @Override
  public abstract BeaconResponse handle(
      cn.hydcraft.hydronyasama.protocol.BeaconMessage message,
      cn.hydcraft.hydronyasama.transport.TransportContext context);
}
