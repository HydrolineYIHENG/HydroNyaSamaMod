package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TileRendererProfile;

public final class HoloJetRev extends LegacyOpticsUnit {
  public HoloJetRev() {
    super("holojetrev");
  }

  public HoloJetRev setPowerMilliWatt(double power) {
    service().setPower(endpoint(), power);
    return this;
  }

  public double powerMilliWatt() {
    return service().power(endpoint(), 80.0D);
  }

  public TileRendererProfile rendererProfile() {
    return service().rendererProfile("holo_jet_rev");
  }
}
