package cn.hydcraft.hydronyasama.optics.compat.tool;

import cn.hydcraft.hydronyasama.optics.compat.api.IOpticalDevice;
import java.util.Locale;

/** Compatibility helper that formats old NSOConv inspect output. */
public final class NSOConv {
  private NSOConv() {}

  public static String inspectColor(int color) {
    return String.format(Locale.ROOT, "[NSO] Color: 0x%08X", color);
  }

  public static String inspectForegroundBackground(int foreground, int background) {
    return String.format(Locale.ROOT, "[NSO] Fore: 0x%08X, Back: 0x%08X", foreground, background);
  }

  public static String inspectOpticsPower(IOpticalDevice device) {
    if (device == null) {
      return "[NSO] I: 0.0 mW, O: 0.0 mW";
    }
    return String.format(
        Locale.ROOT, "[NSO] I: %.1f mW, O: %.1f mW", device.mWInput(), device.mWOutput());
  }
}
