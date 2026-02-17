package cn.hydcraft.hydronyasama.optics.compat.legacy;

public final class CreativeTabLoader {
  private static final CreativeTabLoader INSTANCE = new CreativeTabLoader();

  private CreativeTabLoader() {}

  public static CreativeTabLoader instance() {
    return INSTANCE;
  }
}