package cn.hydcraft.hydronyasama.service.mtr;

import cn.hydcraft.hydronyasama.mtr.MtrQueryGateway;
import cn.hydcraft.hydronyasama.mtr.MtrQueryRegistry;
import cn.hydcraft.hydronyasama.protocol.BeaconMessage;
import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import cn.hydcraft.hydronyasama.protocol.ResultCode;
import com.google.gson.JsonObject;

abstract class AbstractMtrActionHandler
    implements cn.hydcraft.hydronyasama.service.BeaconActionHandler {
  protected MtrQueryGateway gateway() {
    return MtrQueryRegistry.get();
  }

  protected BeaconResponse notReady(String requestId) {
    return BeaconResponse.builder(requestId)
        .result(ResultCode.NOT_READY)
        .message("MTR data not available")
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

  protected BeaconResponse busy(String requestId, String reason) {
    return BeaconResponse.builder(requestId).result(ResultCode.BUSY).message(reason).build();
  }

  protected BeaconResponse error(String requestId, String reason) {
    return BeaconResponse.builder(requestId).result(ResultCode.ERROR).message(reason).build();
  }

  @Override
  public abstract BeaconResponse handle(
      BeaconMessage message, cn.hydcraft.hydronyasama.transport.TransportContext context);
}
