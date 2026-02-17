package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;

public final class ToolHandler {
  private ToolHandler() {}

  public static TelecomCommService.ConnectResult connect(
      LegacyTelecomTileEntity source, LegacyTelecomTileEntity target) {
    if (source == null) {
      return TelecomCommService.ConnectResult.INCOMPATIBLE;
    }
    return source.connectTo(target);
  }

  public static String manualUse(LegacyTelecomTileEntity endpoint, boolean sneaking) {
    return endpoint == null ? "missing" : endpoint.manualUse(sneaking);
  }

  public static void editor(LegacyTelecomTileEntity endpoint, int mode, boolean inverterEnabled) {
    if (endpoint != null) {
      endpoint.applyEditorState(mode, inverterEnabled);
    }
  }

  public static String snapshot(LegacyTelecomTileEntity endpoint) {
    return endpoint == null ? "missing" : endpoint.describe();
  }
}
