package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TileRendererProfile;

public final class TextWall extends LegacyOpticsUnit {
  public TextWall() {
    super("textwall");
  }

  public TextWall setText(String text) {
    service().setText(endpoint(), text);
    return this;
  }

  public String text() {
    return service().text(endpoint());
  }

  public TileRendererProfile rendererProfile() {
    return service().rendererProfile("text_wall");
  }
}
