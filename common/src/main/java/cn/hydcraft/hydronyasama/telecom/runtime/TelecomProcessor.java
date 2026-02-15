package cn.hydcraft.hydronyasama.telecom.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

/**
 * Cross-loader telecom runtime processor migrated from the 1.12.2 implementation.
 *
 * <p>This processor no longer depends on world/tile lookups. Loader-side code registers
 * receiver/transmitter endpoints via callbacks.
 */
public final class TelecomProcessor {

  @FunctionalInterface
  public interface BoolSink {
    void accept(boolean value);
  }

  public enum State {
    SUP,
    ZERO,
    ONE
  }

  public static final class DeviceInfo {
    private final String id;
    private final String key;
    private final String dimension;
    private final int x;
    private final int y;
    private final int z;

    private DeviceInfo(String id, String key, String dimension, int x, int y, int z) {
      this.id = id;
      this.key = key;
      this.dimension = dimension;
      this.x = x;
      this.y = y;
      this.z = z;
    }

    public String id() {
      return id;
    }

    public String key() {
      return key;
    }

    public String dimension() {
      return dimension;
    }

    public int x() {
      return x;
    }

    public int y() {
      return y;
    }

    public int z() {
      return z;
    }
  }

  private static final class ReceiverEndpoint {
    private final DeviceInfo info;
    private final BoolSink sink;

    private ReceiverEndpoint(DeviceInfo info, BoolSink sink) {
      this.info = info;
      this.sink = sink;
    }
  }

  private static final class TransmitterEndpoint {
    private final DeviceInfo info;
    private final BooleanSupplier source;

    private TransmitterEndpoint(DeviceInfo info, BooleanSupplier source) {
      this.info = info;
      this.source = source;
    }
  }

  private final ReentrantLock lock = new ReentrantLock();
  private final Map<String, DeviceInfo> devices = new HashMap<>();
  private final Map<String, ReceiverEndpoint> receivers = new HashMap<>();
  private final Map<String, TransmitterEndpoint> transmitters = new HashMap<>();
  private final Map<String, State> receiverStates = new HashMap<>();
  private final Map<String, State> transmitterStates = new HashMap<>();
  private final Map<String, Integer> innerInputs = new HashMap<>();
  private final Map<String, Boolean> innerOutputs = new HashMap<>();

  public DeviceInfo registerReceiver(
      String id,
      String key,
      String dimension,
      int x,
      int y,
      int z,
      BoolSink sink) {
    Objects.requireNonNull(sink, "sink");
    DeviceInfo info = new DeviceInfo(requireId(id), requireKey(key), safeDimension(dimension), x, y, z);
    lock.lock();
    try {
      devices.put(info.id(), info);
      receivers.put(info.id(), new ReceiverEndpoint(info, sink));
      receiverStates.put(info.id(), State.SUP);
      transmitters.remove(info.id());
      transmitterStates.remove(info.id());
      return info;
    } finally {
      lock.unlock();
    }
  }

  public DeviceInfo registerTransmitter(
      String id,
      String key,
      String dimension,
      int x,
      int y,
      int z,
      BooleanSupplier source) {
    Objects.requireNonNull(source, "source");
    DeviceInfo info = new DeviceInfo(requireId(id), requireKey(key), safeDimension(dimension), x, y, z);
    lock.lock();
    try {
      devices.put(info.id(), info);
      transmitters.put(info.id(), new TransmitterEndpoint(info, source));
      transmitterStates.put(info.id(), State.ZERO);
      receivers.remove(info.id());
      receiverStates.remove(info.id());
      return info;
    } finally {
      lock.unlock();
    }
  }

  public void unregister(String id) {
    lock.lock();
    try {
      devices.remove(id);
      receivers.remove(id);
      transmitters.remove(id);
      receiverStates.remove(id);
      transmitterStates.remove(id);
    } finally {
      lock.unlock();
    }
  }

  public DeviceInfo device(String id) {
    lock.lock();
    try {
      return devices.get(id);
    } finally {
      lock.unlock();
    }
  }

  public boolean isReceiver(DeviceInfo info) {
    if (info == null) {
      return false;
    }
    lock.lock();
    try {
      return receivers.containsKey(info.id());
    } finally {
      lock.unlock();
    }
  }

  public boolean isTransmitter(DeviceInfo info) {
    if (info == null) {
      return false;
    }
    lock.lock();
    try {
      return transmitters.containsKey(info.id());
    } finally {
      lock.unlock();
    }
  }

  public void set(DeviceInfo info, boolean state) {
    if (info == null) {
      return;
    }
    lock.lock();
    try {
      if (receiverStates.containsKey(info.id())) {
        receiverStates.put(info.id(), state ? State.ONE : State.ZERO);
      }
    } finally {
      lock.unlock();
    }
  }

  public boolean get(DeviceInfo info) {
    if (info == null) {
      return false;
    }
    lock.lock();
    try {
      State state = transmitterStates.get(info.id());
      return state == State.ONE;
    } finally {
      lock.unlock();
    }
  }

  public void set(String key, boolean state) {
    lock.lock();
    try {
      int value = innerInputs.containsKey(key) ? innerInputs.get(key) : 0;
      innerInputs.put(key, value + (state ? 1 : -1));
    } finally {
      lock.unlock();
    }
  }

  public boolean get(String key) {
    lock.lock();
    try {
      Boolean value = innerOutputs.get(key);
      return value != null && value;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Runs one logic tick:
   *
   * <ul>
   *   <li>Pushes pending receiver states to registered receiver callbacks.
   *   <li>Polls transmitter callbacks into state cache.
   *   <li>Compacts inner key bus accumulator into boolean outputs.
   * </ul>
   */
  public void update() {
    if (!lock.tryLock()) {
      return;
    }
    try {
      for (Map.Entry<String, ReceiverEndpoint> entry : receivers.entrySet()) {
        State state = receiverStates.get(entry.getKey());
        if (state == null || state == State.SUP) {
          continue;
        }
        entry.getValue().sink.accept(state == State.ONE);
        receiverStates.put(entry.getKey(), State.SUP);
      }

      for (Map.Entry<String, TransmitterEndpoint> entry : transmitters.entrySet()) {
        boolean powered = entry.getValue().source.getAsBoolean();
        transmitterStates.put(entry.getKey(), powered ? State.ONE : State.ZERO);
      }

      for (Map.Entry<String, Integer> entry : innerInputs.entrySet()) {
        innerOutputs.put(entry.getKey(), entry.getValue() > 0);
      }
      innerInputs.clear();
    } finally {
      lock.unlock();
    }
  }

  private static String requireId(String id) {
    if (id == null || id.trim().isEmpty() || "null".equals(id)) {
      throw new IllegalArgumentException("device id is invalid");
    }
    return id;
  }

  private static String requireKey(String key) {
    if (key == null || key.trim().isEmpty() || "null".equals(key)) {
      throw new IllegalArgumentException("device key is invalid");
    }
    return key;
  }

  private static String safeDimension(String dimension) {
    return dimension == null || dimension.trim().isEmpty() ? "unknown" : dimension;
  }
}
