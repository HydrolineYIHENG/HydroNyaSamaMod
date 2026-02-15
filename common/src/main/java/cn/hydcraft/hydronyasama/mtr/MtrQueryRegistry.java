package cn.hydcraft.hydronyasama.mtr;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global registry so loader-specific entrypoints can provide an {@link MtrQueryGateway}
 * implementation that will be consumed by the cross-loader action handlers.
 */
public final class MtrQueryRegistry {
  private static final AtomicReference<MtrQueryGateway> GATEWAY =
      new AtomicReference<>(MtrQueryGateway.UNAVAILABLE);

  private MtrQueryRegistry() {}

  public static MtrQueryGateway get() {
    return GATEWAY.get();
  }

  public static void register(MtrQueryGateway gateway) {
    Objects.requireNonNull(gateway, "gateway");
    GATEWAY.set(gateway);
  }
}
