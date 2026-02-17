package cn.hydcraft.hydronyasama.optics.compat.legacy;

public final class ItemLoader {
  private static final ItemLoader INSTANCE = new ItemLoader();

  private ItemLoader() {}

  public static ItemLoader instance() {
    return INSTANCE;
  }
}