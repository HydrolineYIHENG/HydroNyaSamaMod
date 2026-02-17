package cn.hydcraft.hydronyasama.optics.compat.font;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight compatibility font registry.
 *
 * <p>Legacy binary font assets are loader/client specific; this class keeps a common-side lookup
 * API for text model building.
 */
public final class FontLoader {
  public static final int FONT_LISHU = 0;
  public static final int FONT_SONG = 1;
  public static final int FONT_KAI = 2;
  public static final int FONT_HEI = 3;
  public static final int FONT_LONG = 4;
  public static final int FONT_YAN = 5;

  public static final int ALIGN_NULL = 0;
  public static final int ALIGN_NOP = 1;
  public static final int ALIGN_LEFT = 2;
  public static final int ALIGN_RIGHT = 3;
  public static final int ALIGN_CENTER = 4;
  public static final int ALIGN_UP = 5;
  public static final int ALIGN_DOWN = 6;

  private static final Map<Integer, byte[]> FONT_DATA = new ConcurrentHashMap<>();

  private FontLoader() {}

  public static void registerFont(int font, byte[] data) {
    FONT_DATA.put(font, data == null ? new byte[0] : data.clone());
  }

  public static byte[] fontData(int font) {
    byte[] bytes = FONT_DATA.get(font);
    return bytes == null ? new byte[0] : bytes.clone();
  }

  public static TextModel getModel(int font, int align, String text, int thick, int color) {
    return new TextModel(fontData(font), align, text, thick, color);
  }
}
