package cn.hydcraft.hydronyasama.optics.compat.api;

/** Compatibility interface matching legacy input/output optical power semantics. */
public interface IOpticalDevice {
  double mWInput();

  double mWOutput();
}
