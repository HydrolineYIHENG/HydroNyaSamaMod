package cn.hydcraft.hydronyasama.core.train;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Client-side compatibility handler for train sync packets. */
public final class TrainControlClientHandler {
  private final Map<String, TrainControlState> stateByTrainId = new ConcurrentHashMap<>();

  public void handle(TrainPacket packet) {
    if (packet.type() != TrainPacket.Type.SYNC) {
      return;
    }
    TrainControlState state = new TrainControlState();
    state.setDirection(packet.direction());
    state.setPower(packet.power());
    state.setBrakeResistance(packet.brakeResistance());
    state.setVelocity(packet.velocity());
    stateByTrainId.put(normalizeId(packet.trainId()), state);
  }

  public TrainControlState snapshot(String trainId) {
    TrainControlState state = stateByTrainId.get(normalizeId(trainId));
    if (state == null) {
      return new TrainControlState();
    }
    TrainControlState out = new TrainControlState();
    out.setDirection(state.getDirection());
    out.setPower(state.getPower());
    out.setBrakeResistance(state.getBrakeResistance());
    out.setVelocity(state.getVelocity());
    return out;
  }

  private static String normalizeId(String trainId) {
    return trainId == null || trainId.trim().isEmpty() ? "default" : trainId.trim();
  }
}
