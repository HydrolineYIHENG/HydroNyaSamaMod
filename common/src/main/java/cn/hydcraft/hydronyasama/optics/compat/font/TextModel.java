package cn.hydcraft.hydronyasama.optics.compat.font;

import java.util.Locale;

/** Headless compatibility text model used by legacy optics render bridge. */
public final class TextModel {
  private final byte[] fontData;
  private final int align;
  private final String text;
  private final int thick;
  private final int color;

  public TextModel(byte[] fontData, int align, String text, int thick, int color) {
    this.fontData = fontData == null ? new byte[0] : fontData.clone();
    this.align = align;
    this.text = text == null ? "" : text;
    this.thick = Math.max(1, thick);
    this.color = color;
  }

  public byte[] fontData() {
    return fontData.clone();
  }

  public int align() {
    return align;
  }

  public String text() {
    return text;
  }

  public int thick() {
    return thick;
  }

  public int color() {
    return color;
  }

  public String normalizedText() {
    String compact = text.replace("\r", "");
    if (align == FontLoader.ALIGN_UP || align == FontLoader.ALIGN_DOWN) {
      return compact.replace(" ", "\n");
    }
    return compact;
  }

  public String debugSummary() {
    return String.format(
        Locale.ROOT,
        "TextModel[len=%d,align=%d,thick=%d,color=0x%08X,font=%d]",
        text.length(),
        align,
        thick,
        color,
        fontData.length);
  }
}
