package cn.hydcraft.hydronyasama.optics.compat.legacy;

public final class TileEntityModelBinder {
  private static final TileEntityModelBinder INSTANCE = new TileEntityModelBinder();

  private TileEntityModelBinder() {}

  public static TileEntityModelBinder instance() {
    return INSTANCE;
  }
}