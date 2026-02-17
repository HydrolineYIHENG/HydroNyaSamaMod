package cn.hydcraft.hydronyasama.optics.compat.legacy;

public final class ClientProxy {
  private static final ClientProxy INSTANCE = new ClientProxy();

  private ClientProxy() {}

  public static ClientProxy instance() {
    return INSTANCE;
  }
}