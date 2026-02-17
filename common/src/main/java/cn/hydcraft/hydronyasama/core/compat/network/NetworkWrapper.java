package cn.hydcraft.hydronyasama.core.compat.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Compatibility message bus used to mirror legacy simple network wrapper flows. */
public final class NetworkWrapper {
  private final Map<String, Consumer<String>> handlers = new ConcurrentHashMap<>();

  public void register(String channel, Consumer<String> handler) {
    if (channel == null || channel.trim().isEmpty() || handler == null) {
      return;
    }
    handlers.put(channel.trim(), handler);
  }

  public void remove(String channel) {
    if (channel == null) {
      return;
    }
    handlers.remove(channel.trim());
  }

  public void emit(String channel, String payload) {
    if (channel == null) {
      return;
    }
    Consumer<String> handler = handlers.get(channel.trim());
    if (handler != null) {
      handler.accept(payload == null ? "" : payload);
    }
  }
}
