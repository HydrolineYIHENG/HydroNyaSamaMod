package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TileRendererProfile;

public final class LEDPlate extends LegacyOpticsUnit {
  public LEDPlate() {
    super("ledplate");
  }

  public LEDPlate setForeground(int color) {
    service().setColor(endpoint(), color);
    return this;
  }

  public LEDPlate setBackground(int color) {
    service().setBackColor(endpoint(), color);
    return this;
  }

  public int foreground() {
    return service().color(endpoint(), 0xFFFFFFFF);
  }

  public int background() {
    return service().backColor(endpoint(), 0xFF000000);
  }

  public TileRendererProfile rendererProfile() {
    return service().rendererProfile("led_plate");
  }
}
