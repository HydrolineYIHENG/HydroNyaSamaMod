package cn.hydcraft.hydronyasama.telecom.runtime;

import cn.hydcraft.hydronyasama.telecom.signal.SignalBoxState;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Loader-agnostic telecom logic runtime.
 *
 * <p>It simulates key-bus based components (input/output/signal box/delayer/timer) and provides
 * deterministic tick updates for loader-side block entities.
 */
public final class TelecomRuntime {

  private abstract static class Component {
    final String id;
    final String key;
    final TelecomComponentKind kind;
    boolean pendingInput;
    boolean output;

    Component(String id, String key, TelecomComponentKind kind) {
      this.id = id;
      this.key = key;
      this.kind = kind;
    }

    void setPendingInput(boolean state) {
      this.pendingInput = state;
    }

    abstract void tick();
  }

  private static final class InputComponent extends Component {
    private boolean sourceState;

    InputComponent(String id, String key) {
      super(id, key, TelecomComponentKind.INPUT);
    }

    void setSourceState(boolean sourceState) {
      this.sourceState = sourceState;
    }

    @Override
    void tick() {
      output = sourceState;
    }
  }

  private static final class OutputComponent extends Component {
    private boolean sinkState;

    OutputComponent(String id, String key) {
      super(id, key, TelecomComponentKind.OUTPUT);
    }

    boolean sinkState() {
      return sinkState;
    }

    @Override
    void tick() {
      sinkState = pendingInput;
      output = false;
    }
  }

  private static final class SignalBoxComponent extends Component {
    private final SignalBoxState state = new SignalBoxState();

    SignalBoxComponent(String id, String key) {
      super(id, key, TelecomComponentKind.SIGNAL_BOX);
      state.setSourceConnected(true);
    }

    void setSourceConnected(boolean connected) {
      state.setSourceConnected(connected);
    }

    void setInverterEnabled(boolean enabled) {
      state.setInverterEnabled(enabled);
    }

    @Override
    void tick() {
      state.setSourcePowered(pendingInput);
      output = state.tick();
    }
  }

  private static final class DelayerComponent extends Component {
    private int delayTicks;
    private final Deque<Boolean> queue = new ArrayDeque<Boolean>();

    DelayerComponent(String id, String key, int delayTicks) {
      super(id, key, TelecomComponentKind.DELAYER);
      this.delayTicks = Math.max(1, delayTicks);
      for (int i = 0; i < this.delayTicks; i++) {
        queue.add(Boolean.FALSE);
      }
    }

    void setDelayTicks(int delayTicks) {
      int normalized = Math.max(1, delayTicks);
      if (normalized == this.delayTicks) {
        return;
      }
      this.delayTicks = normalized;
      queue.clear();
      for (int i = 0; i < this.delayTicks; i++) {
        queue.add(Boolean.FALSE);
      }
    }

    @Override
    void tick() {
      queue.add(Boolean.valueOf(pendingInput));
      while (queue.size() > delayTicks) {
        queue.removeFirst();
      }
      output = queue.isEmpty() ? false : queue.removeFirst().booleanValue();
    }
  }

  private static final class TimerComponent extends Component {
    private int periodTicks;
    private int counter;

    TimerComponent(String id, String key, int periodTicks) {
      super(id, key, TelecomComponentKind.TIMER);
      this.periodTicks = Math.max(1, periodTicks);
      this.counter = 0;
    }

    void setPeriodTicks(int periodTicks) {
      this.periodTicks = Math.max(1, periodTicks);
      if (counter >= this.periodTicks) {
        counter = 0;
      }
    }

    @Override
    void tick() {
      if (!pendingInput) {
        counter = 0;
        output = false;
        return;
      }
      counter++;
      if (counter >= periodTicks) {
        counter = 0;
        output = !output;
      }
    }
  }

  public static final class Snapshot {
    public final String id;
    public final String key;
    public final TelecomComponentKind kind;
    public final boolean input;
    public final boolean output;

    Snapshot(String id, String key, TelecomComponentKind kind, boolean input, boolean output) {
      this.id = id;
      this.key = key;
      this.kind = kind;
      this.input = input;
      this.output = output;
    }
  }

  private final Map<String, Component> components = new HashMap<String, Component>();
  private final Map<String, Integer> externalBusInputs = new HashMap<String, Integer>();

  public void registerInput(String id, String key) {
    put(new InputComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key")));
  }

  public void registerOutput(String id, String key) {
    put(new OutputComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key")));
  }

  public void registerSignalBox(String id, String key) {
    put(new SignalBoxComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key")));
  }

  public void registerDelayer(String id, String key, int delayTicks) {
    put(new DelayerComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key"), delayTicks));
  }

  public void registerTimer(String id, String key, int periodTicks) {
    put(new TimerComponent(requireNonEmpty(id, "id"), requireNonEmpty(key, "key"), periodTicks));
  }

  public void unregister(String id) {
    components.remove(id);
  }

  public void setInputSourceState(String id, boolean state) {
    Component component = requiredComponent(id, TelecomComponentKind.INPUT);
    ((InputComponent) component).setSourceState(state);
  }

  public boolean getOutputSinkState(String id) {
    Component component = requiredComponent(id, TelecomComponentKind.OUTPUT);
    return ((OutputComponent) component).sinkState();
  }

  public void setSignalBoxConnected(String id, boolean connected) {
    Component component = requiredComponent(id, TelecomComponentKind.SIGNAL_BOX);
    ((SignalBoxComponent) component).setSourceConnected(connected);
  }

  public void setSignalBoxInverter(String id, boolean enabled) {
    Component component = requiredComponent(id, TelecomComponentKind.SIGNAL_BOX);
    ((SignalBoxComponent) component).setInverterEnabled(enabled);
  }

  public void setDelayerTicks(String id, int delayTicks) {
    Component component = requiredComponent(id, TelecomComponentKind.DELAYER);
    ((DelayerComponent) component).setDelayTicks(delayTicks);
  }

  public void setTimerPeriodTicks(String id, int periodTicks) {
    Component component = requiredComponent(id, TelecomComponentKind.TIMER);
    ((TimerComponent) component).setPeriodTicks(periodTicks);
  }

  /**
   * Sets external key-bus input delta. Matches old telecom processor semantics:
   *
   * <ul>
   *   <li>{@code true} increments key weight
   *   <li>{@code false} decrements key weight
   * </ul>
   */
  public void setExternalBusInput(String key, boolean state) {
    String normalized = requireNonEmpty(key, "key");
    int old =
        externalBusInputs.containsKey(normalized)
            ? externalBusInputs.get(normalized).intValue()
            : 0;
    externalBusInputs.put(normalized, Integer.valueOf(old + (state ? 1 : -1)));
  }

  public boolean getExternalBusOutput(String key) {
    Integer value = externalBusInputs.get(key);
    return value != null && value.intValue() > 0;
  }

  public void tick() {
    Map<String, Boolean> busSignals = new HashMap<String, Boolean>();
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

    for (Component component : components.values()) {
      Boolean input = busSignals.get(component.key);
      component.setPendingInput(input != null && input.booleanValue());
    }

    for (Component component : components.values()) {
      component.tick();
    }

    externalBusInputs.clear();
  }

  public List<Snapshot> snapshots() {
    List<Snapshot> snapshots = new ArrayList<Snapshot>(components.size());
    for (Component component : components.values()) {
      snapshots.add(
          new Snapshot(
              component.id,
              component.key,
              component.kind,
              component.pendingInput,
              component.output));
    }
    Collections.sort(
        snapshots,
        new java.util.Comparator<Snapshot>() {
          @Override
          public int compare(Snapshot o1, Snapshot o2) {
            return o1.id.compareTo(o2.id);
          }
        });
    return snapshots;
  }

  private void put(Component component) {
    Component old = components.put(component.id, component);
    if (old != null && !Objects.equals(old.key, component.key)) {
      externalBusInputs.remove(old.key);
    }
  }

  private Component requiredComponent(String id, TelecomComponentKind kind) {
    Component component = components.get(id);
    if (component == null) {
      throw new IllegalArgumentException("component not found: " + id);
    }
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
}
