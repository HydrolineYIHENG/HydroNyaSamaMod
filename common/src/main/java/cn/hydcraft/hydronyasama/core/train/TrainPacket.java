package cn.hydcraft.hydronyasama.core.train;

import java.util.Objects;

/** Legacy-compatible train control payload used by migrated handlers. */
public final class TrainPacket {
  public enum Type {
    CONTROL,
    SYNC
  }

  private final Type type;
  private final String trainId;
  private final int direction;
  private final int power;
  private final int brakeResistance;
  private final double velocity;

  public TrainPacket(
      Type type, String trainId, int direction, int power, int brakeResistance, double velocity) {
    this.type = Objects.requireNonNull(type, "type");
    this.trainId = trainId == null ? "" : trainId;
    this.direction = direction;
    this.power = power;
    this.brakeResistance = brakeResistance;
    this.velocity = velocity;
  }

  public Type type() {
    return type;
  }

  public String trainId() {
    return trainId;
  }

  public int direction() {
    return direction;
  }

  public int power() {
    return power;
  }

  public int brakeResistance() {
    return brakeResistance;
  }

  public double velocity() {
    return velocity;
  }

  public static TrainPacket control(
      String trainId, int direction, int power, int brakeResistance, double velocity) {
    return new TrainPacket(Type.CONTROL, trainId, direction, power, brakeResistance, velocity);
  }

  public static TrainPacket sync(
      String trainId, int direction, int power, int brakeResistance, double velocity) {
    return new TrainPacket(Type.SYNC, trainId, direction, power, brakeResistance, velocity);
  }

  public String encode() {
    return type.name()
        + "|"
        + trainId
        + "|"
        + direction
        + "|"
        + power
        + "|"
        + brakeResistance
        + "|"
        + velocity;
  }

  public static TrainPacket decode(String encoded) {
    if (encoded == null || encoded.isEmpty()) {
      return sync("", 0, 0, TrainControlState.MAX_BRAKE_RESISTANCE, 0.0D);
    }
    String[] parts = encoded.split("\\|", -1);
    if (parts.length < 6) {
      return sync("", 0, 0, TrainControlState.MAX_BRAKE_RESISTANCE, 0.0D);
    }
    Type type = parseType(parts[0]);
    return new TrainPacket(
        type,
        parts[1],
        parseInt(parts[2], 0),
        parseInt(parts[3], 0),
        parseInt(parts[4], TrainControlState.MAX_BRAKE_RESISTANCE),
        parseDouble(parts[5], 0.0D));
  }

  private static Type parseType(String raw) {
    try {
      return Type.valueOf(raw);
    } catch (RuntimeException ignored) {
      return Type.SYNC;
    }
  }

  private static int parseInt(String raw, int fallback) {
    try {
      return Integer.parseInt(raw);
    } catch (RuntimeException ignored) {
      return fallback;
    }
  }

  private static double parseDouble(String raw, double fallback) {
    try {
      return Double.parseDouble(raw);
    } catch (RuntimeException ignored) {
      return fallback;
    }
  }
}
