package cn.hydcraft.hydronyasama.service;

import cn.hydcraft.hydronyasama.protocol.BeaconMessage;
import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import cn.hydcraft.hydronyasama.protocol.ResultCode;
import cn.hydcraft.hydronyasama.transport.TransportContext;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.time.Instant;

/** Simple built-in action that helps Bukkit check connectivity and latency. */
public final class PingActionHandler implements BeaconActionHandler {
  public static final String ACTION = "beacon:ping";

  @Override
  public String action() {
    return ACTION;
  }

  @Override
  public BeaconResponse handle(BeaconMessage message, TransportContext context) {
    JsonObject payload = new JsonObject();
    payload.addProperty(
        "echo",
        message.getPayload().has("echo") ? message.getPayload().get("echo").getAsString() : "pong");
    payload.addProperty("receivedAt", context.getReceivedAt().toEpochMilli());
    payload.addProperty(
        "latencyMs", Duration.between(context.getReceivedAt(), Instant.now()).abs().toMillis());
    return BeaconResponse.builder(message.getRequestId())
        .result(ResultCode.OK)
        .payload(payload)
        .build();
  }
}
