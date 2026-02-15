package cn.hydcraft.hydronyasama.core.train;

/** Loader-agnostic control state used by migrated NTP controllers. */
public final class TrainControlState {
  public static final int MAX_POWER = 20;
  public static final int MIN_BRAKE_RESISTANCE = 1;
  public static final int MAX_BRAKE_RESISTANCE = 10;

  private int direction;
  private int power;
  private int brakeResistance = MAX_BRAKE_RESISTANCE;
  private double velocity;

  public int getDirection() {
    return direction;
  }

  public void setDirection(int direction) {
    if (direction < -1 || direction > 1) {
      throw new IllegalArgumentException("direction must be -1, 0 or 1");
    }
    this.direction = direction;
  }

  public int getPower() {
    return power;
  }

  public void setPower(int power) {
    this.power = clamp(power, 0, MAX_POWER);
  }

  public int getBrakeResistance() {
    return brakeResistance;
  }

  public void setBrakeResistance(int brakeResistance) {
    this.brakeResistance = clamp(brakeResistance, MIN_BRAKE_RESISTANCE, MAX_BRAKE_RESISTANCE);
  }

  public double getVelocity() {
    return velocity;
  }

  public void setVelocity(double velocity) {
    this.velocity = Math.max(0.0D, velocity);
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
