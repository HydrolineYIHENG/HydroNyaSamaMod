package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TileRendererProfile;

public final class GuideBoard extends LegacyOpticsUnit {
  public GuideBoard() {
    super("guideboard");
  }

  public GuideBoard setText(String text) {
    service().setText(endpoint(), text);
    return this;
  }

  public String text() {
    return service().text(endpoint());
  }

  public TileRendererProfile rendererProfile() {
    return service().rendererProfile("guide_board");
  }
}
