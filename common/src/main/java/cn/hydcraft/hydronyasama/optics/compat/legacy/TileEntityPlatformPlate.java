package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TileRendererProfile;

public final class TileEntityPlatformPlate extends LegacyOpticsUnit {
  public TileEntityPlatformPlate() {
    super("tileentityplatformplate");
  }

  public TileEntityPlatformPlate setColor(int color) {
    service().setColor(endpoint(), color);
    return this;
  }

  public int color() {
    return service().color(endpoint(), 0xFFFFFFFF);
  }

  public TileRendererProfile rendererProfile() {
    return service().rendererProfile("platform_plate");
  }
}
