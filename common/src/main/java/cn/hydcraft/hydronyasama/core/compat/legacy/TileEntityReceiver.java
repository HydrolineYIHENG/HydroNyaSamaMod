package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommRuntime;

public final class TileEntityReceiver extends LegacyTelecomTileEntity {
  public TileEntityReceiver() {
    super("signal_box");
  }

  public TileEntityReceiver(String endpoint) {
    super(endpoint, "signal_box");
  }

  public boolean isEnabled() {
    TelecomCommRuntime.Snapshot snapshot = snapshot();
    return snapshot != null && snapshot.enabled;
  }

  public void setEnabled(boolean enabled) {
    if (enabled != isEnabled()) {
      manualUse(false);
    }
  }
}
