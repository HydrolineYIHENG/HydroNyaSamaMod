package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.api.ILightSource;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TileRendererProfile;

public final class RGBLight extends LegacyOpticsUnit implements ILightSource {
  public RGBLight() {
    super("rgblight");
  }

  public RGBLight setColor(int color) {
    service().setColor(endpoint(), color);
    return this;
  }

  public RGBLight setPowerMilliWatt(double power) {
    service().setPower(endpoint(), power);
    return this;
  }

  public TileRendererProfile rendererProfile() {
    return service().rendererProfile("light");
  }

  @Override
  public int lightColor() {
    return service().color(endpoint(), 0xFFFFFFFF);
  }

  @Override
  public double lightPowerMilliWatt() {
    return service().power(endpoint(), 120.0D);
  }
}
