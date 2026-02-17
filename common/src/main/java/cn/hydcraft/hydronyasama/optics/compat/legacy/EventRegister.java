package cn.hydcraft.hydronyasama.optics.compat.legacy;

public final class EventRegister {
  private static final EventRegister INSTANCE = new EventRegister();

  private EventRegister() {}

  public static EventRegister instance() {
    return INSTANCE;
  }
}