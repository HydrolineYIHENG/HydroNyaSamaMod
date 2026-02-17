package cn.hydcraft.hydronyasama.optics.compat.util;

import cn.hydcraft.hydronyasama.optics.compat.font.FontLoader;
import cn.hydcraft.hydronyasama.optics.compat.font.TextModel;

/** Compatibility facade for decorative text model creation. */
public final class DecoTextCore {
  private DecoTextCore() {}

  public static TextModel build(int font, int align, String text, int thick, int color) {
    return FontLoader.getModel(font, align, text, thick, color);
  }
}
