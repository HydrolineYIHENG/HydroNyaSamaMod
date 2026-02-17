package cn.hydcraft.hydronyasama.optics.compat.legacy;

public final class NyaSamaOptics {
  private static final NyaSamaOptics INSTANCE = new NyaSamaOptics();

  private NyaSamaOptics() {}

  public static NyaSamaOptics instance() {
    return INSTANCE;
  }
}