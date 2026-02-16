package cn.hydcraft.hydronyasama.electricity.runtime;

import cn.hydcraft.hydronyasama.core.physics.Vec3;

/** Utility math migrated from the legacy electricity module. */
public final class ElectricityMath {

  private static final double DEG_TO_RAD = Math.PI / 180.0D;

  private ElectricityMath() {}

  public static float cosAngle(float angle) {
    return (float) Math.cos(angle * DEG_TO_RAD);
  }

  public static float sinAngle(float angle) {
    return (float) Math.sin(angle * DEG_TO_RAD);
  }

  public static float asinh(float x) {
    return (float) Math.log(x + Math.sqrt(x * x + 1.0F));
  }

  public static float acosh(float x) {
    return (float) Math.log(x + Math.sqrt(x * x - 1.0F));
  }

  public static float atanh(float x) {
    return (float) (0.5D * Math.log((1.0F + x) / (1.0F - x)));
  }

  public static double distanceOf(
      double xStart, double yStart, double zStart, double xEnd, double yEnd, double zEnd) {
    return Math.sqrt(
        (xStart - xEnd) * (xStart - xEnd)
            + (yStart - yEnd) * (yStart - yEnd)
            + (zStart - zEnd) * (zStart - zEnd));
  }

  public static double distanceOf(Vec3 start, Vec3 end) {
    return distanceOf(start.x, start.y, start.z, end.x, end.y, end.z);
  }
}
