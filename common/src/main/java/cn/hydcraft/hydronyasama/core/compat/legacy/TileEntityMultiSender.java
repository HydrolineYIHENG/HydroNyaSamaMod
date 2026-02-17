package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TileEntityMultiSender extends LegacyTelecomTileEntity {
  private final List<String> targets = new ArrayList<String>();

  public TileEntityMultiSender() {
    super("signal_box_sender");
  }

  public TileEntityMultiSender(String endpoint) {
    super(endpoint, "signal_box_sender");
  }

  public TelecomCommService.ConnectResult connectTarget(LegacyTelecomTileEntity target) {
    TelecomCommService.ConnectResult result = connectTo(target);
    if (result != TelecomCommService.ConnectResult.INCOMPATIBLE && target != null) {
      if (!targets.contains(target.endpoint())) {
        targets.add(target.endpoint());
      }
    }
    return result;
  }

  public List<String> targets() {
    return Collections.unmodifiableList(targets);
  }

  public void trigger() {
    manualUse(false);
  }
}
