package cn.hydcraft.hydronyasama.optics.compat.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Compatibility network wrapper with message-id dispatch semantics. */
public final class NetworkWrapper {
  private final Map<Integer, Consumer<byte[]>> listeners = new ConcurrentHashMap<>();

  public void register(int id, Consumer<byte[]> listener) {
    if (listener != null) {
      listeners.put(id, listener);
    }
  }

  public void unregister(int id) {
    listeners.remove(id);
  }

  public void emit(int id, byte[] payload) {
    Consumer<byte[]> listener = listeners.get(id);
    if (listener != null) {
      listener.accept(payload == null ? new byte[0] : payload);
    }
  }
}
