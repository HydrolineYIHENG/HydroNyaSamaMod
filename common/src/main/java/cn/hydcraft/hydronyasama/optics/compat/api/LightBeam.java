package cn.hydcraft.hydronyasama.optics.compat.api;

/** Immutable compatibility implementation of a light beam. */
public final class LightBeam implements ILightBeam {
  private final int color;
  private final double powerMilliWatt;
  private final double length;

  public LightBeam(int color, double powerMilliWatt, double length) {
    this.color = color;
    this.powerMilliWatt = powerMilliWatt;
    this.length = Math.max(0.0D, length);
  }

  @Override
  public int color() {
    return color;
  }

  @Override
  public double powerMilliWatt() {
    return powerMilliWatt;
  }

  @Override
  public double length() {
    return length;
  }
}
