package cn.hydcraft.hydronyasama.telecom.runtime;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Full telecom communication runtime migrated from legacy NyaSamaTelecom behavior.
 *
 * <p>This runtime is loader-agnostic and only models deterministic communication/state transitions.
 * Loader-side BlockEntity code should bind in-world links (sender/target/transceiver) and
 * synchronize user operations to this runtime.
 */
public final class TelecomCommRuntime {

  public enum Kind {
    INPUT,
    OUTPUT,
    SIGNAL_BOX,
    SIGNAL_BOX_SENDER,
    SIGNAL_BOX_GETTER,
    TRI_STATE_SIGNAL_BOX,
    RS_LATCH,
    TIMER,
    DELAYER,
    WIRELESS_RX,
    WIRELESS_TX
  }

  private abstract static class Component {
    final String id;
    final String key;
    final Kind kind;
    String senderId;
    String targetId;
    String transceiverId;
    boolean pendingInput;
    boolean output;
    boolean enabled;
    boolean inverterEnabled;

    Component(String id, String key, Kind kind) {
      this.id = id;
      this.key = key;
      this.kind = kind;
    }

    abstract void tick(TelecomCommRuntime runtime);

    void acceptControl(boolean state) {
      enabled = state;
    }

    void acceptTriState(int triState) {
      // default NOP
    }
  }

  private static final class InputComponent extends Component {
    boolean sourceState;

    InputComponent(String id, String key) {
      super(id, key, Kind.INPUT);
    }

    @Override
    void tick(TelecomCommRuntime runtime) {
      output = sourceState;
      enabled = sourceState;
    }
  }

  private static final class OutputComponent extends Component {
    boolean sinkState;

    OutputComponent(String id, String key) {
      super(id, key, Kind.OUTPUT);
    }

    @Override
    void tick(TelecomCommRuntime runtime) {
      sinkState = pendingInput;
      output = false;
      enabled = sinkState;
    }
  }

  private static final class SignalBoxComponent extends Component {
    SignalBoxComponent(String id, String key) {
      super(id, key, Kind.SIGNAL_BOX);
    }

    @Override
    void tick(TelecomCommRuntime runtime) {
      boolean senderLinked = senderId != null;
      boolean source = senderLinked ? pendingInput : enabled;
      enabled = source;
      boolean controlState = inverterEnabled ? !source : source;
      runtime.emitControl(targetId, controlState);
      output = source;
    }
  }

  private static class SenderComponent extends Component {
    SenderComponent(String id, String key, Kind kind) {
      super(id, key, kind);
    }

    @Override
    void tick(TelecomCommRuntime runtime) {
      boolean transceiverPowered = runtime.isTransceiverPowered(transceiverId);
      output = enabled && transceiverPowered;
    }
  }

  private static class GetterComponent extends Component {
    GetterComponent(String id, String key, Kind kind) {
      super(id, key, kind);
    }

    @Override
    void tick(TelecomCommRuntime runtime) {
      boolean senderLinked = senderId != null;
      if (senderLinked) {
        enabled = pendingInput;
      }
      boolean transceiverPowered = runtime.isTransceiverPowered(transceiverId);
      output = enabled && transceiverPowered;
    }
  }

  private static final class TriStateSignalBoxComponent extends Component {
    boolean triStateIsNeg;

    TriStateSignalBoxComponent(String id, String key) {
      super(id, key, Kind.TRI_STATE_SIGNAL_BOX);
    }

    @Override
    void tick(TelecomCommRuntime runtime) {
      boolean senderLinked = senderId != null;
      boolean source = senderLinked ? pendingInput : enabled;
      enabled = source;
      boolean controlState = inverterEnabled ? !source : source;
      if (controlState) {
        runtime.emitTriState(targetId, triStateIsNeg ? -1 : 1);
      }
      output = source;
    }
  }

  private static final class RSLatchComponent extends SenderComponent {
    static final int STATE_POS = 1;
    static final int STATE_ZERO = 0;
    static final int STATE_NEG = -1;
    int triState = STATE_ZERO;

    RSLatchComponent(String id, String key) {
      super(id, key, Kind.RS_LATCH);
    }

    @Override
    void acceptTriState(int state) {
      if (state > 0) {
        triState = STATE_POS;
      } else if (state < 0) {
        triState = STATE_NEG;
      } else {
        triState = STATE_ZERO;
      }
    }

    @Override
    void tick(TelecomCommRuntime runtime) {
      if (triState == STATE_POS) {
        enabled = true;
      } else if (triState == STATE_NEG) {
        enabled = false;
      }
      triState = STATE_ZERO;
      super.tick(runtime);
    }
  }

  private static final class TimerComponent extends SenderComponent {
    static final int STATE_POS = 1;
    static final int STATE_ZERO = 0;
    static final int STATE_NEG = -1;
    int setTime = 20;
    int tmpTime;
    boolean autoReload = true;
    int triState = STATE_ZERO;

    TimerComponent(String id, String key) {
      super(id, key, Kind.TIMER);
    }

    @Override
    void acceptTriState(int state) {
      if (state > 0) {
        triState = STATE_POS;
      } else if (state < 0) {
        triState = STATE_NEG;
      } else {
        triState = STATE_ZERO;
      }
    }

    @Override
    void tick(TelecomCommRuntime runtime) {
      if (triState == STATE_POS) {
        if (tmpTime < setTime) {
          tmpTime++;
        }
      } else if (triState == STATE_NEG) {
        tmpTime = 0;
      }
      if (tmpTime >= setTime) {
        enabled = true;
        if (autoReload) {
          tmpTime = 0;
        }
      } else {
        enabled = false;
      }
      triState = STATE_ZERO;
      super.tick(runtime);
    }
  }

  private static final class DelayerComponent extends Component {
    int setTime = 20;
    int tmpTime;

    DelayerComponent(String id, String key) {
      super(id, key, Kind.DELAYER);
    }

    @Override
    void tick(TelecomCommRuntime runtime) {
      boolean senderLinked = senderId != null;
      boolean source = senderLinked ? pendingInput : enabled;
      enabled = source;
      if (!source) {
        tmpTime = 0;
        runtime.emitControl(targetId, inverterEnabled);
      } else {
        tmpTime++;
        if (tmpTime >= setTime) {
          tmpTime = 0;
          runtime.emitControl(targetId, !inverterEnabled);
        }
      }
      output = source;
    }
  }

  private static final class WirelessRxComponent extends SenderComponent {
    String deviceId = "null";
    String deviceKey = "null";

    WirelessRxComponent(String id, String key) {
      super(id, key, Kind.WIRELESS_RX);
    }
  }

  private static final class WirelessTxComponent extends GetterComponent {
    String deviceId = "null";
    String deviceKey = "null";

    WirelessTxComponent(String id, String key) {
      super(id, key, Kind.WIRELESS_TX);
    }
  }

  public static final class Snapshot {
    public final String id;
    public final String key;
    public final Kind kind;
    public final String senderId;
    public final String targetId;
    public final String transceiverId;
    public final boolean input;
    public final boolean output;
    public final boolean enabled;

    Snapshot(Component component) {
      this.id = component.id;
      this.key = component.key;
      this.kind = component.kind;
      this.senderId = component.senderId;
      this.targetId = component.targetId;
      this.transceiverId = component.transceiverId;
      this.input = component.pendingInput;
      this.output = component.output;
      this.enabled = component.enabled;
    }
  }

  private final LinkedHashMap<String, Component> components =
      new LinkedHashMap<String, Component>();
  private final HashMap<String, Integer> externalBusInputs = new HashMap<String, Integer>();
  private final HashMap<String, Boolean> wirelessRxPending = new HashMap<String, Boolean>();

  public void registerInput(String id, String key) {
    put(new InputComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key")));
  }

  public void registerOutput(String id, String key) {
    put(new OutputComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key")));
  }

  public void registerSignalBox(String id, String key) {
    put(new SignalBoxComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key")));
  }

  public void registerSignalBoxSender(String id, String key) {
    put(
        new SenderComponent(
            requireNonEmpty(id, "id"), requireNonEmpty(key, "key"), Kind.SIGNAL_BOX_SENDER));
  }

  public void registerSignalBoxGetter(String id, String key) {
    put(
        new GetterComponent(
            requireNonEmpty(id, "id"), requireNonEmpty(key, "key"), Kind.SIGNAL_BOX_GETTER));
  }

  public void registerTriStateSignalBox(String id, String key) {
    put(new TriStateSignalBoxComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key")));
  }

  public void registerRSLatch(String id, String key) {
    put(new RSLatchComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key")));
  }

  public void registerTimer(String id, String key) {
    put(new TimerComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key")));
  }

  public void registerDelayer(String id, String key) {
    put(new DelayerComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key")));
  }

  public void registerWirelessRx(String id, String key, String deviceId, String deviceKey) {
    WirelessRxComponent component =
        new WirelessRxComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key"));
    component.deviceId = requireNonEmpty(deviceId, "deviceId");
    component.deviceKey = requireNonEmpty(deviceKey, "deviceKey");
    put(component);
  }

  public void registerWirelessTx(String id, String key, String deviceId, String deviceKey) {
    WirelessTxComponent component =
        new WirelessTxComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key"));
    component.deviceId = requireNonEmpty(deviceId, "deviceId");
    component.deviceKey = requireNonEmpty(deviceKey, "deviceKey");
    put(component);
  }

  public void unregister(String id) {
    components.remove(id);
    for (Component component : components.values()) {
      if (id != null && id.equals(component.senderId)) {
        component.senderId = null;
      }
      if (id != null && id.equals(component.targetId)) {
        component.targetId = null;
      }
      if (id != null && id.equals(component.transceiverId)) {
        component.transceiverId = null;
      }
    }
  }

  public void linkSender(String id, String senderId) {
    required(id).senderId = normalizeNullable(senderId);
  }

  public void linkTarget(String id, String targetId) {
    required(id).targetId = normalizeNullable(targetId);
  }

  public void linkTransceiver(String id, String transceiverId) {
    required(id).transceiverId = normalizeNullable(transceiverId);
  }

  public void setInputSourceState(String id, boolean state) {
    Component component = requiredKind(id, Kind.INPUT);
    ((InputComponent) component).sourceState = state;
  }

  public boolean getOutputSinkState(String id) {
    Component component = requiredKind(id, Kind.OUTPUT);
    return ((OutputComponent) component).sinkState;
  }

  public void setEnabled(String id, boolean enabled) {
    required(id).enabled = enabled;
  }

  public void setInverter(String id, boolean enabled) {
    required(id).inverterEnabled = enabled;
  }

  public void setTriStatePolarityNegative(String id, boolean negative) {
    Component component = requiredKind(id, Kind.TRI_STATE_SIGNAL_BOX);
    ((TriStateSignalBoxComponent) component).triStateIsNeg = negative;
  }

  public void setDelayerTicks(String id, int ticks) {
    Component component = requiredKind(id, Kind.DELAYER);
    ((DelayerComponent) component).setTime = Math.max(0, ticks);
  }

  public void setTimerTicks(String id, int ticks) {
    Component component = requiredKind(id, Kind.TIMER);
    ((TimerComponent) component).setTime = Math.max(0, ticks);
  }

  public void setTimerAutoReload(String id, boolean autoReload) {
    Component component = requiredKind(id, Kind.TIMER);
    ((TimerComponent) component).autoReload = autoReload;
  }

  public void triggerTriStatePos(String id) {
    required(id).acceptTriState(1);
  }

  public void triggerTriStateNeg(String id) {
    required(id).acceptTriState(-1);
  }

  public void setExternalBusInput(String key, boolean state) {
    String normalized = requireNonEmpty(key, "key");
    int old =
        externalBusInputs.containsKey(normalized)
            ? externalBusInputs.get(normalized).intValue()
            : 0;
    externalBusInputs.put(normalized, Integer.valueOf(old + (state ? 1 : -1)));
  }

  public boolean getExternalBusOutput(String key) {
    for (Component component : components.values()) {
      if (Objects.equals(component.key, key) && component.output) {
        return true;
      }
    }
    return false;
  }

  public boolean toWirelessRx(String deviceId, String deviceKey, boolean state) {
    for (Component component : components.values()) {
      if (component instanceof WirelessRxComponent) {
        WirelessRxComponent rx = (WirelessRxComponent) component;
        if (rx.deviceId.equals(deviceId) && rx.deviceKey.equals(deviceKey)) {
          wirelessRxPending.put(rx.id, Boolean.valueOf(state));
          return true;
        }
      }
    }
    return false;
  }

  public Boolean fromWirelessTx(String deviceId, String deviceKey) {
    for (Component component : components.values()) {
      if (component instanceof WirelessTxComponent) {
        WirelessTxComponent tx = (WirelessTxComponent) component;
        if (tx.deviceId.equals(deviceId) && tx.deviceKey.equals(deviceKey)) {
          return Boolean.valueOf(tx.output);
        }
      }
    }
    return null;
  }

  public void tick() {
    HashMap<String, Boolean> outputs = new HashMap<String, Boolean>();
    for (Component component : components.values()) {
      outputs.put(component.id, Boolean.valueOf(component.output));
    }

    HashMap<String, Boolean> busSignals = new HashMap<String, Boolean>();
    for (Component component : components.values()) {
      if (component.output) {
        busSignals.put(component.key, Boolean.TRUE);
      } else if (!busSignals.containsKey(component.key)) {
        busSignals.put(component.key, Boolean.FALSE);
      }
    }
    for (Map.Entry<String, Integer> entry : externalBusInputs.entrySet()) {
      if (entry.getValue().intValue() > 0) {
        busSignals.put(entry.getKey(), Boolean.TRUE);
      } else if (!busSignals.containsKey(entry.getKey())) {
        busSignals.put(entry.getKey(), Boolean.FALSE);
      }
    }
    for (Map.Entry<String, Boolean> entry : wirelessRxPending.entrySet()) {
      Component component = components.get(entry.getKey());
      if (component instanceof WirelessRxComponent) {
        component.enabled = entry.getValue().booleanValue();
      }
    }

    for (Component component : components.values()) {
      boolean fromBus =
          busSignals.containsKey(component.key) && busSignals.get(component.key).booleanValue();
      if (component.senderId != null && outputs.containsKey(component.senderId)) {
        component.pendingInput = outputs.get(component.senderId).booleanValue();
      } else {
        component.pendingInput = fromBus;
      }
    }

    for (Component component : components.values()) {
      component.tick(this);
    }

    externalBusInputs.clear();
    wirelessRxPending.clear();
  }

  public Map<String, Snapshot> snapshots() {
    LinkedHashMap<String, Snapshot> result = new LinkedHashMap<String, Snapshot>();
    for (Component component : components.values()) {
      result.put(component.id, new Snapshot(component));
    }
    return result;
  }

  private boolean isTransceiverPowered(String transceiverId) {
    if (transceiverId == null) {
      return true;
    }
    Component transceiver = components.get(transceiverId);
    return transceiver != null && transceiver.output;
  }

  private void emitControl(String targetId, boolean state) {
    if (targetId == null) {
      return;
    }
    Component target = components.get(targetId);
    if (target != null) {
      target.acceptControl(state);
    }
  }

  private void emitTriState(String targetId, int triState) {
    if (targetId == null) {
      return;
    }
    Component target = components.get(targetId);
    if (target != null) {
      target.acceptTriState(triState);
    }
  }

  private void put(Component component) {
    components.put(component.id, component);
  }

  private Component required(String id) {
    Component component = components.get(id);
    if (component == null) {
      throw new IllegalArgumentException("component not found: " + id);
    }
    return component;
  }

  private Component requiredKind(String id, Kind kind) {
    Component component = required(id);
    if (component.kind != kind) {
      throw new IllegalArgumentException(
          "component kind mismatch, expected " + kind + " but got " + component.kind);
    }
    return component;
  }

  private static String requireNonEmpty(String value, String field) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(field + " is empty");
    }
    return value;
  }

  private static String normalizeNullable(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
