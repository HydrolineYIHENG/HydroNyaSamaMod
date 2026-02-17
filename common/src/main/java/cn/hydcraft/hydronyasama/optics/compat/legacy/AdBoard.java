package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TileRendererProfile;

public final class AdBoard extends LegacyOpticsUnit {
  public AdBoard() {
    super("adboard");
  }

  public AdBoard setText(String text) {
    service().setText(endpoint(), text);
    return this;
  }

  public String text() {
    return service().text(endpoint());
  }

  public TileRendererProfile rendererProfile() {
    return service().rendererProfile("ad_board");
  }
}
