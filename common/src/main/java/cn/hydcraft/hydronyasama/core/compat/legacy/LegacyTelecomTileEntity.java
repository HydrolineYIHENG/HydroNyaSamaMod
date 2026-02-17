package cn.hydcraft.hydronyasama.core.compat.legacy;

import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommRuntime;
import cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService;
import java.util.concurrent.atomic.AtomicLong;

/** Shared legacy tile compatibility adapter backed by the common telecom runtime. */
public abstract class LegacyTelecomTileEntity {
  private static final AtomicLong NEXT_ID = new AtomicLong(1L);

  private final TelecomCommService service;
  private final String endpoint;
  private final String blockPath;

  protected LegacyTelecomTileEntity(String blockPath) {
    this(nextEndpoint(blockPath), blockPath, TelecomCommService.getInstance());
  }

  protected LegacyTelecomTileEntity(String endpoint, String blockPath) {
    this(endpoint, blockPath, TelecomCommService.getInstance());
  }

  protected LegacyTelecomTileEntity(String endpoint, String blockPath, TelecomCommService service) {
    this.service = service == null ? TelecomCommService.getInstance() : service;
    this.endpoint = normalize(endpoint, "endpoint");
    this.blockPath = normalize(blockPath, "blockPath");
    this.service.ensureComponent(this.endpoint, this.blockPath);
  }

  public final String endpoint() {
    return endpoint;
  }

  public final String blockPath() {
    return blockPath;
  }

  public final TelecomCommRuntime.Snapshot snapshot() {
    return service.snapshot(endpoint, blockPath);
  }

  public final String describe() {
    return TelecomCommService.describeSnapshot(snapshot());
  }

  public final void tick() {
    service.tick();
  }

  public final String manualUse(boolean sneaking) {
    return service.handleManualUse(endpoint, blockPath, sneaking);
  }

  public final TelecomCommService.ConnectResult connectTo(LegacyTelecomTileEntity target) {
    if (target == null) {
      return TelecomCommService.ConnectResult.INCOMPATIBLE;
    }
    return service.connect(endpoint, blockPath, target.endpoint(), target.blockPath());
  }

  public final TelecomCommService.ConnectResult connectTo(String targetEndpoint, String targetBlock) {
    return service.connect(endpoint, blockPath, targetEndpoint, targetBlock);
  }

  public final void applyEditorState(int mode, boolean inverterEnabled) {
    service.applyEditorState(endpoint, blockPath, mode, inverterEnabled);
  }

  public final void setRedstoneInput(boolean powered) {
    service.setInputFromRedstone(endpoint, blockPath, powered);
  }

  public final boolean redstoneOutputPowered() {
    return service.redstoneOutputPowered(endpoint, blockPath);
  }

  protected final TelecomCommService service() {
    return service;
  }

  protected static String nextEndpoint(String blockPath) {
    return normalize(blockPath, "blockPath") + "#" + NEXT_ID.getAndIncrement();
  }

  private static String normalize(String value, String field) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(field + " is empty");
    }
    return value.trim();
  }
}
