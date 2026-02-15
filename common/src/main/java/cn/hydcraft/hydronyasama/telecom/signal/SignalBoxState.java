package cn.hydcraft.hydronyasama.telecom.signal;

/**
 * Loader-agnostic signal-box state machine fragment migrated from legacy telecom behavior.
 *
 * <p>This class intentionally keeps only deterministic state transitions. BlockEntity-specific I/O
 * (targets, redstone, wireless packets) should be layered on top per loader/version.
 */
public final class SignalBoxState {

  private boolean sourcePowered;
  private boolean sourceConnected;
  private boolean inverterEnabled;
  private boolean enabled;

  public boolean isSourcePowered() {
    return sourcePowered;
  }

  public void setSourcePowered(boolean sourcePowered) {
    this.sourcePowered = sourcePowered;
  }

  public boolean isSourceConnected() {
    return sourceConnected;
  }

  public void setSourceConnected(boolean sourceConnected) {
    this.sourceConnected = sourceConnected;
  }

  public boolean isInverterEnabled() {
    return inverterEnabled;
  }

  public void setInverterEnabled(boolean inverterEnabled) {
    this.inverterEnabled = inverterEnabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  /** Advances one update tick. Returns the output state that should be written to target device. */
  public boolean tick() {
    boolean output = sourceConnected ? sourcePowered : enabled;
    if (inverterEnabled) {
      output = !output;
    }
    enabled = output;
    return output;
  }
}
