package cn.hydcraft.hydronyasama.optics.compat.renderer.tileblock;

/** Shared compatibility metadata for legacy optics tile renderers. */
public class TileRendererProfile {
  private final String id;
  private final float scale;
  private final boolean emissive;

  protected TileRendererProfile(String id, float scale, boolean emissive) {
    this.id = id;
    this.scale = scale;
    this.emissive = emissive;
  }

  public String id() {
    return id;
  }

  public float scale() {
    return scale;
  }

  public boolean emissive() {
    return emissive;
  }
}
