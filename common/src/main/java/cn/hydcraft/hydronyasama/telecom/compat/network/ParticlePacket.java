package cn.hydcraft.hydronyasama.telecom.compat.network;

import java.util.Locale;

/** Lightweight compatibility payload matching legacy particle packet semantics. */
public final class ParticlePacket {
  public final String type;
  public final double x;
  public final double y;
  public final double z;
  public final double tX;
  public final double tY;
  public final double tZ;

  public ParticlePacket(
      String type, double x, double y, double z, double tX, double tY, double tZ) {
    this.type = type == null ? "" : type;
    this.x = x;
    this.y = y;
    this.z = z;
    this.tX = tX;
    this.tY = tY;
    this.tZ = tZ;
  }

  public String encode() {
    return String.format(Locale.ROOT, "%s|%s|%s|%s|%s|%s|%s", type, x, y, z, tX, tY, tZ);
  }

  public static ParticlePacket decode(String value) {
    if (value == null || value.isEmpty()) {
      return new ParticlePacket("", 0, 0, 0, 0, 0, 0);
    }
    String[] parts = value.split("\\|", -1);
    if (parts.length != 7) {
      return new ParticlePacket("", 0, 0, 0, 0, 0, 0);
    }
    return new ParticlePacket(
        parts[0],
        parseDouble(parts[1]),
        parseDouble(parts[2]),
        parseDouble(parts[3]),
        parseDouble(parts[4]),
        parseDouble(parts[5]),
        parseDouble(parts[6]));
  }

  private static double parseDouble(String value) {
    try {
      return Double.parseDouble(value);
    } catch (RuntimeException ignored) {
      return 0.0D;
    }
  }
}
