package cn.hydcraft.hydronyasama.core.compat.legacy;

public final class Util {
  private Util() {}

  public static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  public static String hex(int value) {
    return String.format(java.util.Locale.ROOT, "0x%08X", value);
  }
}