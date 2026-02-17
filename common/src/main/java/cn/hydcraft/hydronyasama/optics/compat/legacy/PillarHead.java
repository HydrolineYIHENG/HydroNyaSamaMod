package cn.hydcraft.hydronyasama.optics.compat.legacy;

import cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock.TileRendererProfile;

public final class PillarHead extends LegacyOpticsUnit {
  public PillarHead() {
    super("pillarhead");
  }

  public TileRendererProfile rendererProfile() {
    return service().rendererProfile("pillar_head");
  }
}
