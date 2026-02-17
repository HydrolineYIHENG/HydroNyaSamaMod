package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.font.TextModel;
import cn.hydcraft.hydronyasama.optics.compat.util.DecoTextCore;

public final class TileEntityHoloText extends LegacyOpticsUnit {
  private int color = 0xFF00FFFF;

  public TileEntityHoloText() {
    super("tileentityholotext");
  }

  public TileEntityHoloText setText(String text) {
    service().setText(endpoint(), text);
    return this;
  }

  public String text() {
    return service().text(endpoint());
  }

  public TileEntityHoloText setColor(int color) {
    this.color = color;
    service().setColor(endpoint(), color);
    return this;
  }

  public int color() {
    return service().color(endpoint(), color);
  }

  public TextModel textModel() {
    return DecoTextCore.build(1, 4, text(), 1, color());
  }
}
