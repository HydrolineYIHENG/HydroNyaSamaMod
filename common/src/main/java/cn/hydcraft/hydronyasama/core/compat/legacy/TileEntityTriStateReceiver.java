package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommRuntime;

public final class TileEntityTriStateReceiver extends LegacyTelecomTileEntity {
  public TileEntityTriStateReceiver() {
    super("tri_state_signal_box");
  }

  public TileEntityTriStateReceiver(String endpoint) {
    super(endpoint, "tri_state_signal_box");
  }

  public void setNegativePolarity(boolean negative) {
    applyEditorState(negative ? 1 : 0, false);
  }

  public boolean outputPowered() {
    TelecomCommRuntime.Snapshot snapshot = snapshot();
    return snapshot != null && snapshot.output;
  }
}
