package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.font.TextModel;
import cn.hydcraft.hydronyasama.optics.compat.util.DecoTextCore;

public final class TileEntityDecoText extends LegacyOpticsUnit {
  private int font = 1;
  private int align = 4;
  private int thick = 1;
  private int color = 0xFFFFFFFF;

  public TileEntityDecoText() {
    super("tileentitydecotext");
  }

  public TileEntityDecoText setStyle(int font, int align, int thick, int color) {
    this.font = font;
    this.align = align;
    this.thick = thick;
    this.color = color;
    return this;
  }

  public TileEntityDecoText setText(String text) {
    service().setText(endpoint(), text);
    return this;
  }

  public String text() {
    return service().text(endpoint());
  }

  public TextModel textModel() {
    return DecoTextCore.build(font, align, text(), thick, color);
  }
}
