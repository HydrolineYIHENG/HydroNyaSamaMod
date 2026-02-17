package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommRuntime;

public final class TileEntitySingleSender extends LegacyTelecomTileEntity {
  public TileEntitySingleSender() {
    super("signal_box_sender");
  }

  public TileEntitySingleSender(String endpoint) {
    super(endpoint, "signal_box_sender");
  }

  public void trigger() {
    manualUse(false);
  }

  public boolean outputPowered() {
    TelecomCommRuntime.Snapshot snapshot = snapshot();
    return snapshot != null && snapshot.output;
  }
}
