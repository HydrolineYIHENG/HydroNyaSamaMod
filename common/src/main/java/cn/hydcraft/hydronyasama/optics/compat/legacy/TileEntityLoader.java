package cn.hydcraft.hydronyasama.optics.compat.legacy;

public final class TileEntityLoader {
  private static final TileEntityLoader INSTANCE = new TileEntityLoader();

  private TileEntityLoader() {}

  public static TileEntityLoader instance() {
    return INSTANCE;
  }
}