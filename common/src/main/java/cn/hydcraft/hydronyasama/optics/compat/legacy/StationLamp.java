package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.api.ILightSource;
import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TileRendererProfile;

public final class StationLamp extends LegacyOpticsUnit implements ILightSource {
  public StationLamp() {
    super("stationlamp");
  }

  public StationLamp setColor(int color) {
    service().setColor(endpoint(), color);
    return this;
  }

  public StationLamp setPowerMilliWatt(double power) {
    service().setPower(endpoint(), power);
    return this;
  }

  public TileRendererProfile rendererProfile() {
    return service().rendererProfile("station_lamp");
  }

  @Override
  public int lightColor() {
    return service().color(endpoint(), 0xFFFFFFFF);
  }

  @Override
  public double lightPowerMilliWatt() {
    return service().power(endpoint(), 100.0D);
  }
}
