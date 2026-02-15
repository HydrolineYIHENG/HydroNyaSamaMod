package cn.hydcraft.hydronyasama.core.train;

/** Legacy-compatible train dynamics formulas extracted for cross-version reuse. */
public final class TrainPhysics {
  private static final double G = 9.8D;

  private TrainPhysics() {}

  public static double calcVelocityUp(
      double velocity, double friction, double mass, double power, double dt) {
    if (velocity <= 0.0D) {
      return 0.0D;
    }
    double force = friction * mass * G;
    double maxAccel = (1.0D - friction) * G;
    double accel = (power - force * velocity) / (velocity * mass);
    if (accel > maxAccel) {
      accel = maxAccel;
    }
    return Math.max(0.0D, velocity + accel * dt);
  }

  public static double calcVelocityDown(
      double velocity,
      double friction,
      double mass,
      double brakeStrength,
      double inductance,
      double resistance,
      double dt) {
    if (velocity <= 0.0D) {
      return 0.0D;
    }
    double force = friction * mass * G;
    double maxAccel = (1.0D + friction) * G;
    double accel =
        (brakeStrength * brakeStrength * inductance * inductance * velocity / resistance + force)
            / mass;
    if (accel > maxAccel) {
      accel = maxAccel;
    }
    if (accel < friction * G) {
      accel = friction * G;
    }
    return Math.max(0.0D, velocity - accel * dt);
  }
}
