package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommRuntime;

public final class TileEntityPassiveReceiver extends LegacyTelecomTileEntity {
  public TileEntityPassiveReceiver() {
    super("signal_box_getter");
  }

  public TileEntityPassiveReceiver(String endpoint) {
    super(endpoint, "signal_box_getter");
  }

  public boolean outputPowered() {
    TelecomCommRuntime.Snapshot snapshot = snapshot();
    return snapshot != null && snapshot.output;
  }
}
