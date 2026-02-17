package cn.hydcraft.hydronyasama.telecom.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared telecom communication service for tool-side operations.
 *
 * <p>It bridges loader item interactions (connector/editor/tablet) to the common telecom runtime.
 */
public final class TelecomCommService {
  public enum ConnectResult {
    CONNECTED,
    DISCONNECTED,
    INCOMPATIBLE
  }

  private static final String DEFAULT_WIRELESS_DEVICE_ID = "default";
  private static final String DEFAULT_WIRELESS_DEVICE_KEY = "default";

  private static final TelecomCommService INSTANCE = new TelecomCommService();

  private final TelecomCommRuntime runtime = new TelecomCommRuntime();
  private final LinkedHashMap<String, TelecomCommRuntime.Kind> kindsByEndpoint =
      new LinkedHashMap<String, TelecomCommRuntime.Kind>();
  private final LinkedHashMap<String, NspgaProfile> nspgaProfilesByEndpoint =
      new LinkedHashMap<String, NspgaProfile>();

  private TelecomCommService() {}

  public static TelecomCommService getInstance() {
    return INSTANCE;
  }

  public synchronized void reset() {
    kindsByEndpoint.clear();
    nspgaProfilesByEndpoint.clear();
  }

  public synchronized void ensureComponent(String endpoint, String blockPath) {
    TelecomCommRuntime.Kind kind = resolveKind(blockPath);
    TelecomCommRuntime.Kind oldKind = kindsByEndpoint.get(endpoint);
    if (oldKind == kind) {
      return;
    }
    if (oldKind != null) {
      runtime.unregister(endpoint);
    }
    register(endpoint, kind);
    kindsByEndpoint.put(endpoint, kind);
    if (!isNspgaBlock(blockPath)) {
      nspgaProfilesByEndpoint.remove(endpoint);
    } else if (!nspgaProfilesByEndpoint.containsKey(endpoint)) {
      nspgaProfilesByEndpoint.put(endpoint, NspgaProfile.empty());
    }
  }

  public synchronized ConnectResult connect(
      String sourceEndpoint,
      String sourceBlockPath,
      String targetEndpoint,
      String targetBlockPath) {
    ensureComponent(sourceEndpoint, sourceBlockPath);
    ensureComponent(targetEndpoint, targetBlockPath);

    TelecomCommRuntime.Kind sourceKind = kindsByEndpoint.get(sourceEndpoint);
    TelecomCommRuntime.Kind targetKind = kindsByEndpoint.get(targetEndpoint);
    if (sourceKind == null || targetKind == null) {
      return ConnectResult.INCOMPATIBLE;
    }

    boolean changed = false;
    boolean disconnected = false;

    if (canSend(sourceKind) && acceptsSender(targetKind)) {
      TelecomCommRuntime.Snapshot targetSnapshot = runtime.snapshots().get(targetEndpoint);
      boolean connected = targetSnapshot != null && sourceEndpoint.equals(targetSnapshot.senderId);
      runtime.linkSender(targetEndpoint, connected ? null : sourceEndpoint);
      changed = true;
      disconnected = disconnected || connected;
    }

    if (acceptsTarget(sourceKind) && canBeTarget(targetKind)) {
      TelecomCommRuntime.Snapshot sourceSnapshot = runtime.snapshots().get(sourceEndpoint);
      boolean connected = sourceSnapshot != null && targetEndpoint.equals(sourceSnapshot.targetId);
      runtime.linkTarget(sourceEndpoint, connected ? null : targetEndpoint);
      changed = true;
      disconnected = disconnected || connected;
    }

    if (usesTransceiver(sourceKind) && usesTransceiver(targetKind)) {
      TelecomCommRuntime.Snapshot sourceSnapshot = runtime.snapshots().get(sourceEndpoint);
      boolean connected =
          sourceSnapshot != null && targetEndpoint.equals(sourceSnapshot.transceiverId);
      if (connected) {
        runtime.linkTransceiver(sourceEndpoint, null);
        runtime.linkTransceiver(targetEndpoint, null);
        disconnected = true;
      } else {
        runtime.linkTransceiver(sourceEndpoint, targetEndpoint);
        runtime.linkTransceiver(targetEndpoint, sourceEndpoint);
      }
      changed = true;
    }

    if (!changed) {
      return ConnectResult.INCOMPATIBLE;
    }

    return disconnected ? ConnectResult.DISCONNECTED : ConnectResult.CONNECTED;
  }

  public synchronized void applyEditorState(
      String endpoint, String blockPath, int mode, boolean inverterEnabled) {
    ensureComponent(endpoint, blockPath);
    TelecomCommRuntime.Kind kind = kindsByEndpoint.get(endpoint);
    if (kind == null) {
      return;
    }

    if (supportsInverter(kind)) {
      runtime.setInverter(endpoint, inverterEnabled);
    }

    if (kind == TelecomCommRuntime.Kind.TRI_STATE_SIGNAL_BOX) {
      runtime.setTriStatePolarityNegative(endpoint, mode == 1);
    } else if (kind == TelecomCommRuntime.Kind.TIMER) {
      runtime.setTimerAutoReload(endpoint, mode != 1);
      runtime.setTimerTicks(endpoint, mode == 2 ? 100 : 20);
      if (mode == 1) {
        runtime.triggerTriStatePos(endpoint);
      } else if (mode == 2) {
        runtime.triggerTriStateNeg(endpoint);
      }
    } else if (kind == TelecomCommRuntime.Kind.DELAYER) {
      runtime.setDelayerTicks(endpoint, mode == 2 ? 40 : 20);
    } else if (kind == TelecomCommRuntime.Kind.RS_LATCH) {
      if (mode == 1) {
        runtime.triggerTriStatePos(endpoint);
      } else if (mode == 2) {
        runtime.triggerTriStateNeg(endpoint);
      }
    }
  }

  public synchronized TelecomCommRuntime.Snapshot snapshot(String endpoint, String blockPath) {
    ensureComponent(endpoint, blockPath);
    return runtime.snapshots().get(endpoint);
  }

  public synchronized Map<String, TelecomCommRuntime.Snapshot> snapshotAll() {
    return Collections.unmodifiableMap(
        new LinkedHashMap<String, TelecomCommRuntime.Snapshot>(runtime.snapshots()));
  }

  public synchronized String handleManualUse(String endpoint, String blockPath, boolean sneaking) {
    ensureComponent(endpoint, blockPath);
    TelecomCommRuntime.Snapshot before = runtime.snapshots().get(endpoint);
    if (before == null) {
      return "missing";
    }

    if (before.kind == TelecomCommRuntime.Kind.INPUT) {
      setInputSourceState(endpoint, !before.enabled);
    } else if (before.kind == TelecomCommRuntime.Kind.SIGNAL_BOX
        || before.kind == TelecomCommRuntime.Kind.SIGNAL_BOX_SENDER
        || before.kind == TelecomCommRuntime.Kind.SIGNAL_BOX_GETTER
        || before.kind == TelecomCommRuntime.Kind.TRI_STATE_SIGNAL_BOX
        || before.kind == TelecomCommRuntime.Kind.DELAYER
        || before.kind == TelecomCommRuntime.Kind.WIRELESS_RX
        || before.kind == TelecomCommRuntime.Kind.WIRELESS_TX) {
      runtime.setEnabled(endpoint, !before.enabled);
    } else if (before.kind == TelecomCommRuntime.Kind.RS_LATCH
        || before.kind == TelecomCommRuntime.Kind.TIMER) {
      if (sneaking) {
        runtime.triggerTriStateNeg(endpoint);
      } else {
        runtime.triggerTriStatePos(endpoint);
      }
    }

    tick();
    TelecomCommRuntime.Snapshot after = runtime.snapshots().get(endpoint);
    return describeSnapshot(after);
  }

  public synchronized void tick() {
    runtime.tick();
  }

  public synchronized void setInputFromRedstone(
      String endpoint, String blockPath, boolean powered) {
    ensureComponent(endpoint, blockPath);
    TelecomCommRuntime.Kind kind = kindsByEndpoint.get(endpoint);
    if (kind != TelecomCommRuntime.Kind.INPUT) {
      return;
    }
    runtime.setInputSourceState(endpoint, powered);
  }

  public synchronized boolean redstoneOutputPowered(String endpoint, String blockPath) {
    ensureComponent(endpoint, blockPath);
    TelecomCommRuntime.Kind kind = kindsByEndpoint.get(endpoint);
    if (kind != TelecomCommRuntime.Kind.OUTPUT) {
      return false;
    }
    TelecomCommRuntime.Snapshot snapshot = runtime.snapshots().get(endpoint);
    return snapshot != null && snapshot.enabled;
  }

  public synchronized void setNspgaIoCount(String endpoint, String blockPath, int ioCount) {
    ensureComponent(endpoint, blockPath);
    if (!isNspgaBlock(blockPath)) {
      return;
    }
    NspgaProfile profile = nspgaProfilesByEndpoint.get(endpoint);
    if (profile == null) {
      profile = NspgaProfile.empty();
    }
    int safe = Math.max(1, ioCount);
    nspgaProfilesByEndpoint.put(
        endpoint, new NspgaProfile(safe, profile.inputs, profile.outputs, profile.code));
  }

  public synchronized void setNspgaInputs(String endpoint, String blockPath, List<String> inputs) {
    ensureComponent(endpoint, blockPath);
    if (!isNspgaBlock(blockPath)) {
      return;
    }
    NspgaProfile profile = nspgaProfilesByEndpoint.get(endpoint);
    if (profile == null) {
      profile = NspgaProfile.empty();
    }
    nspgaProfilesByEndpoint.put(
        endpoint,
        new NspgaProfile(profile.ioCount, sanitizeSignals(inputs), profile.outputs, profile.code));
  }

  public synchronized void setNspgaOutputs(
      String endpoint, String blockPath, List<String> outputs) {
    ensureComponent(endpoint, blockPath);
    if (!isNspgaBlock(blockPath)) {
      return;
    }
    NspgaProfile profile = nspgaProfilesByEndpoint.get(endpoint);
    if (profile == null) {
      profile = NspgaProfile.empty();
    }
    nspgaProfilesByEndpoint.put(
        endpoint,
        new NspgaProfile(profile.ioCount, profile.inputs, sanitizeSignals(outputs), profile.code));
  }

  public synchronized void setNspgaCode(String endpoint, String blockPath, String code) {
    ensureComponent(endpoint, blockPath);
    if (!isNspgaBlock(blockPath)) {
      return;
    }
    NspgaProfile profile = nspgaProfilesByEndpoint.get(endpoint);
    if (profile == null) {
      profile = NspgaProfile.empty();
    }
    nspgaProfilesByEndpoint.put(
        endpoint,
        new NspgaProfile(
            profile.ioCount, profile.inputs, profile.outputs, code == null ? "" : code));
  }

  public synchronized void clearNspgaProfile(String endpoint, String blockPath) {
    ensureComponent(endpoint, blockPath);
    if (!isNspgaBlock(blockPath)) {
      return;
    }
    nspgaProfilesByEndpoint.put(endpoint, NspgaProfile.empty());
  }

  public synchronized NspgaProfile nspgaProfile(String endpoint, String blockPath) {
    ensureComponent(endpoint, blockPath);
    if (!isNspgaBlock(blockPath)) {
      return null;
    }
    NspgaProfile profile = nspgaProfilesByEndpoint.get(endpoint);
    return profile == null ? NspgaProfile.empty() : profile;
  }

  public static String describeNspgaProfile(NspgaProfile profile) {
    if (profile == null) {
      return "nspga:missing";
    }
    return "nspga:io="
        + profile.ioCount
        + ";inputs="
        + String.join(",", profile.inputs)
        + ";outputs="
        + String.join(",", profile.outputs)
        + ";codeLen="
        + profile.code.length();
  }

  public static String describeSnapshot(TelecomCommRuntime.Snapshot snapshot) {
    if (snapshot == null) {
      return "missing";
    }
    return "kind="
        + snapshot.kind
        + ";enabled="
        + snapshot.enabled
        + ";input="
        + snapshot.input
        + ";output="
        + snapshot.output
        + ";sender="
        + (snapshot.senderId == null ? "" : snapshot.senderId)
        + ";target="
        + (snapshot.targetId == null ? "" : snapshot.targetId)
        + ";transceiver="
        + (snapshot.transceiverId == null ? "" : snapshot.transceiverId);
  }

  private void setInputSourceState(String endpoint, boolean state) {
    try {
      runtime.setInputSourceState(endpoint, state);
    } catch (IllegalArgumentException ignored) {
      runtime.setEnabled(endpoint, state);
    }
  }

  private void register(String endpoint, TelecomCommRuntime.Kind kind) {
    if (kind == TelecomCommRuntime.Kind.INPUT) {
      runtime.registerInput(endpoint, endpoint);
    } else if (kind == TelecomCommRuntime.Kind.OUTPUT) {
      runtime.registerOutput(endpoint, endpoint);
    } else if (kind == TelecomCommRuntime.Kind.SIGNAL_BOX) {
      runtime.registerSignalBox(endpoint, endpoint);
    } else if (kind == TelecomCommRuntime.Kind.SIGNAL_BOX_SENDER) {
      runtime.registerSignalBoxSender(endpoint, endpoint);
    } else if (kind == TelecomCommRuntime.Kind.SIGNAL_BOX_GETTER) {
      runtime.registerSignalBoxGetter(endpoint, endpoint);
    } else if (kind == TelecomCommRuntime.Kind.TRI_STATE_SIGNAL_BOX) {
      runtime.registerTriStateSignalBox(endpoint, endpoint);
    } else if (kind == TelecomCommRuntime.Kind.RS_LATCH) {
      runtime.registerRSLatch(endpoint, endpoint);
    } else if (kind == TelecomCommRuntime.Kind.TIMER) {
      runtime.registerTimer(endpoint, endpoint);
    } else if (kind == TelecomCommRuntime.Kind.DELAYER) {
      runtime.registerDelayer(endpoint, endpoint);
    } else if (kind == TelecomCommRuntime.Kind.WIRELESS_RX) {
      runtime.registerWirelessRx(
          endpoint, endpoint, DEFAULT_WIRELESS_DEVICE_ID, DEFAULT_WIRELESS_DEVICE_KEY);
    } else if (kind == TelecomCommRuntime.Kind.WIRELESS_TX) {
      runtime.registerWirelessTx(
          endpoint, endpoint, DEFAULT_WIRELESS_DEVICE_ID, DEFAULT_WIRELESS_DEVICE_KEY);
    }
  }

  private static TelecomCommRuntime.Kind resolveKind(String blockPath) {
    if ("signal_box_input".equals(blockPath)) {
      return TelecomCommRuntime.Kind.INPUT;
    }
    if ("signal_box_output".equals(blockPath)) {
      return TelecomCommRuntime.Kind.OUTPUT;
    }
    if ("signal_box_sender".equals(blockPath)) {
      return TelecomCommRuntime.Kind.SIGNAL_BOX_SENDER;
    }
    if ("signal_box_getter".equals(blockPath)) {
      return TelecomCommRuntime.Kind.SIGNAL_BOX_GETTER;
    }
    if ("tri_state_signal_box".equals(blockPath)) {
      return TelecomCommRuntime.Kind.TRI_STATE_SIGNAL_BOX;
    }
    if ("rs_latch".equals(blockPath)) {
      return TelecomCommRuntime.Kind.RS_LATCH;
    }
    if ("timer".equals(blockPath)) {
      return TelecomCommRuntime.Kind.TIMER;
    }
    if ("delayer".equals(blockPath)) {
      return TelecomCommRuntime.Kind.DELAYER;
    }
    if ("signal_box_rx".equals(blockPath)) {
      return TelecomCommRuntime.Kind.WIRELESS_RX;
    }
    if ("signal_box_tx".equals(blockPath)) {
      return TelecomCommRuntime.Kind.WIRELESS_TX;
    }
    if ("signal_box".equals(blockPath)
        || "nsasm_box".equals(blockPath)
        || blockPath.startsWith("nspga_")) {
      return TelecomCommRuntime.Kind.SIGNAL_BOX;
    }
    return TelecomCommRuntime.Kind.SIGNAL_BOX;
  }

  private static boolean supportsInverter(TelecomCommRuntime.Kind kind) {
    return kind == TelecomCommRuntime.Kind.SIGNAL_BOX
        || kind == TelecomCommRuntime.Kind.TRI_STATE_SIGNAL_BOX
        || kind == TelecomCommRuntime.Kind.DELAYER;
  }

  private static boolean canSend(TelecomCommRuntime.Kind kind) {
    return kind == TelecomCommRuntime.Kind.INPUT
        || kind == TelecomCommRuntime.Kind.SIGNAL_BOX_SENDER
        || kind == TelecomCommRuntime.Kind.SIGNAL_BOX_GETTER
        || kind == TelecomCommRuntime.Kind.RS_LATCH
        || kind == TelecomCommRuntime.Kind.TIMER
        || kind == TelecomCommRuntime.Kind.WIRELESS_RX
        || kind == TelecomCommRuntime.Kind.WIRELESS_TX;
  }

  private static boolean acceptsSender(TelecomCommRuntime.Kind kind) {
    return kind == TelecomCommRuntime.Kind.OUTPUT
        || kind == TelecomCommRuntime.Kind.SIGNAL_BOX
        || kind == TelecomCommRuntime.Kind.SIGNAL_BOX_GETTER
        || kind == TelecomCommRuntime.Kind.TRI_STATE_SIGNAL_BOX
        || kind == TelecomCommRuntime.Kind.DELAYER
        || kind == TelecomCommRuntime.Kind.WIRELESS_TX;
  }

  private static boolean acceptsTarget(TelecomCommRuntime.Kind kind) {
    return kind == TelecomCommRuntime.Kind.SIGNAL_BOX
        || kind == TelecomCommRuntime.Kind.TRI_STATE_SIGNAL_BOX
        || kind == TelecomCommRuntime.Kind.DELAYER;
  }

  private static boolean canBeTarget(TelecomCommRuntime.Kind kind) {
    return kind == TelecomCommRuntime.Kind.SIGNAL_BOX_SENDER
        || kind == TelecomCommRuntime.Kind.SIGNAL_BOX_GETTER
        || kind == TelecomCommRuntime.Kind.RS_LATCH
        || kind == TelecomCommRuntime.Kind.TIMER
        || kind == TelecomCommRuntime.Kind.WIRELESS_RX
        || kind == TelecomCommRuntime.Kind.WIRELESS_TX;
  }

  private static boolean usesTransceiver(TelecomCommRuntime.Kind kind) {
    return kind == TelecomCommRuntime.Kind.SIGNAL_BOX_SENDER
        || kind == TelecomCommRuntime.Kind.SIGNAL_BOX_GETTER
        || kind == TelecomCommRuntime.Kind.RS_LATCH
        || kind == TelecomCommRuntime.Kind.TIMER
        || kind == TelecomCommRuntime.Kind.WIRELESS_RX
        || kind == TelecomCommRuntime.Kind.WIRELESS_TX;
  }

  private static boolean isNspgaBlock(String blockPath) {
    return blockPath != null && blockPath.startsWith("nspga_");
  }

  private static List<String> sanitizeSignals(List<String> values) {
    ArrayList<String> sanitized = new ArrayList<String>();
    if (values == null) {
      return sanitized;
    }
    for (int i = 0; i < values.size(); i++) {
      String value = values.get(i);
      if (value == null) {
        continue;
      }
      String trimmed = value.trim();
      if (!trimmed.isEmpty()) {
        sanitized.add(trimmed);
      }
    }
    return sanitized;
  }

  public static final class NspgaProfile {
    public final int ioCount;
    public final List<String> inputs;
    public final List<String> outputs;
    public final String code;

    private NspgaProfile(int ioCount, List<String> inputs, List<String> outputs, String code) {
      this.ioCount = Math.max(1, ioCount);
      this.inputs = Collections.unmodifiableList(new ArrayList<String>(inputs));
      this.outputs = Collections.unmodifiableList(new ArrayList<String>(outputs));
      this.code = code == null ? "" : code;
    }

    private static NspgaProfile empty() {
      return new NspgaProfile(
          1, Collections.<String>emptyList(), Collections.<String>emptyList(), "");
    }
  }
}
