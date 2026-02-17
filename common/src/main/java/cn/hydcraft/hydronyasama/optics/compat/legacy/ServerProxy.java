package cn.hydcraft.hydronyasama.optics.compat.legacy;

public final class ServerProxy {
  private static final ServerProxy INSTANCE = new ServerProxy();

  private ServerProxy() {}

  public static ServerProxy instance() {
    return INSTANCE;
  }
}