package cn.hydcraft.hydronyasama.core.compat.legacy;

public final class TileEntityActuator extends LegacyTelecomTileEntity {
  public TileEntityActuator() {
    super("signal_box_output");
  }

  public TileEntityActuator(String endpoint) {
    super(endpoint, "signal_box_output");
  }

  public boolean isActive() {
    return redstoneOutputPowered();
  }
}
