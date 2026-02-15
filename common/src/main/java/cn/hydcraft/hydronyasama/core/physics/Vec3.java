package cn.hydcraft.hydronyasama.core.physics;

public final class Vec3 {
  public final double x;
  public final double y;
  public final double z;

  public Vec3(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public double length() {
    return Math.sqrt(x * x + y * y + z * z);
  }
}
