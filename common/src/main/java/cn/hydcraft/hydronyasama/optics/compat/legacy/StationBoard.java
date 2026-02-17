package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TileRendererProfile;

public final class StationBoard extends LegacyOpticsUnit {
  public StationBoard() {
    super("stationboard");
  }

  public StationBoard setText(String text) {
    service().setText(endpoint(), text);
    return this;
  }

  public String text() {
    return service().text(endpoint());
  }

  public TileRendererProfile rendererProfile() {
    return service().rendererProfile("station_board");
  }
}
