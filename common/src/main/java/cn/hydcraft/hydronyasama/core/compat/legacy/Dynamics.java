package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.core.train.TrainPhysics;

public final class Dynamics {
  private Dynamics() {}

  public static double velocityUp(double velocity, double power, double dt) {
    return TrainPhysics.calcVelocityUp(velocity, 0.03D, 40000.0D, power, dt);
  }

  public static double velocityDown(double velocity, double brakeResistance, double dt) {
    return TrainPhysics.calcVelocityDown(velocity, 0.03D, 40000.0D, 25.0D, 0.8D, brakeResistance, dt);
  }
}