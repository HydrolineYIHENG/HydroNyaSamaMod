package cn.hydcraft.hydronyasama.optics.compat.api;

/** Compatibility interface for an optical beam value object. */
public interface ILightBeam {
  int color();

  double powerMilliWatt();

  double length();
}
