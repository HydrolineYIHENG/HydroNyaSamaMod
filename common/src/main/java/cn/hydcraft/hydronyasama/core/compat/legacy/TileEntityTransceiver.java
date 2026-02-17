package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommRuntime;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;

public final class TileEntityTransceiver extends LegacyTelecomTileEntity {
  public TileEntityTransceiver() {
    super("signal_box_sender");
  }

  public TileEntityTransceiver(String endpoint) {
    super(endpoint, "signal_box_sender");
  }

  public TelecomCommService.ConnectResult pair(TileEntityTransceiver other) {
    return connectTo(other);
  }

  public boolean transceiverIsPowered() {
    TelecomCommRuntime.Snapshot snapshot = snapshot();
    return snapshot != null && snapshot.output;
  }
}
