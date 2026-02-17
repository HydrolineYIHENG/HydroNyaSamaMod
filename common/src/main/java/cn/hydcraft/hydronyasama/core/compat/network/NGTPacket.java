package cn.hydcraft.hydronyasama.core.compat.network;

import java.util.Locale;

/** Compatibility payload mirroring legacy NGT packet fields. */
public final class NGTPacket {
  public final String targetId;
  public final String code;

  public NGTPacket(String targetId, String code) {
    this.targetId = targetId == null ? "" : targetId;
    this.code = code == null ? "" : code;
  }

  public String encode() {
    String escaped = code.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n");
    return String.format(Locale.ROOT, "%s|%s", targetId, escaped);
  }

  public static NGTPacket decode(String payload) {
    if (payload == null || payload.isEmpty()) {
      return new NGTPacket("", "");
    }
    int split = payload.indexOf('|');
    if (split < 0) {
      return new NGTPacket(payload, "");
    }
    String target = payload.substring(0, split);
    String escaped = payload.substring(split + 1);
    String code = escaped.replace("\\n", "\n").replace("\\|", "|").replace("\\\\", "\\");
    return new NGTPacket(target, code);
  }
}
