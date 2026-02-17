package cn.hydcraft.hydronyasama.optics.compat.legacy;

public final class BlockLoader {
  private static final BlockLoader INSTANCE = new BlockLoader();

  private BlockLoader() {}

  public static BlockLoader instance() {
    return INSTANCE;
  }
}