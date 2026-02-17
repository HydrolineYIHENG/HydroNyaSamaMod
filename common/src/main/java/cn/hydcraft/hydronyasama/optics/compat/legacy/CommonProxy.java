package cn.hydcraft.hydronyasama.optics.compat.legacy;

public final class CommonProxy {
  private static final CommonProxy INSTANCE = new CommonProxy();

  private CommonProxy() {}

  public static CommonProxy instance() {
    return INSTANCE;
  }
}