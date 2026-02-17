package cn.hydcraft.hydronyasama.optics.compat.api;

/** Compatibility view of a legacy optical light source. */
public interface ILightSource {
  int lightColor();

  double lightPowerMilliWatt();
}
