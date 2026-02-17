package cn.hydcraft.hydronyasama.core.train;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Shared train controller that keeps per-train state and simulation helpers. */
public final class TrainController {
  private final Map<String, TrainControlState> stateByTrainId = new ConcurrentHashMap<>();

  public TrainControlState getOrCreate(String trainId) {
    String id = normalizeId(trainId);
    return stateByTrainId.computeIfAbsent(id, key -> new TrainControlState());
  }

  public TrainControlState apply(TrainPacket packet) {
    TrainControlState state = getOrCreate(packet.trainId());
    state.setDirection(packet.direction());
    state.setPower(packet.power());
    state.setBrakeResistance(packet.brakeResistance());
    state.setVelocity(packet.velocity());
    return copyOf(state);
  }

  public TrainControlState tick(
      String trainId,
      double mass,
      double friction,
      double tractionPower,
      double brakeStrength,
      double inductance,
      double resistance,
      double dt) {
    TrainControlState state = getOrCreate(trainId);
    double velocity = state.getVelocity();
    if (state.getDirection() == 0 || state.getPower() <= 0) {
      velocity =
          TrainPhysics.calcVelocityDown(
              velocity, friction, mass, brakeStrength, inductance, resistance, dt);
    } else {
      double scaledPower =
          tractionPower * (state.getPower() / (double) TrainControlState.MAX_POWER);
      velocity = TrainPhysics.calcVelocityUp(velocity, friction, mass, scaledPower, dt);
    }
    state.setVelocity(velocity);
    return copyOf(state);
  }

  public TrainControlState snapshot(String trainId) {
    return copyOf(getOrCreate(trainId));
  }

  public void reset() {
    stateByTrainId.clear();
  }

  private static String normalizeId(String trainId) {
    return trainId == null || trainId.trim().isEmpty() ? "default" : trainId.trim();
  }

  private static TrainControlState copyOf(TrainControlState src) {
    TrainControlState out = new TrainControlState();
    out.setDirection(src.getDirection());
    out.setPower(src.getPower());
    out.setBrakeResistance(src.getBrakeResistance());
    out.setVelocity(src.getVelocity());
    return out;
  }
}
