package cn.hydcraft.hydronyasama.telecom.compat.event;

import java.util.HashMap;
import java.util.Map;

/** Lightweight chunk-ticket tracker used as a compatibility replacement. */
public final class ChunkLoaderHandler {
  private final Map<String, Integer> tickets = new HashMap<String, Integer>();

  public synchronized void acquire(String dimension, int chunkX, int chunkZ) {
    String key = key(dimension, chunkX, chunkZ);
    Integer count = tickets.get(key);
    tickets.put(key, Integer.valueOf(count == null ? 1 : count.intValue() + 1));
  }

  public synchronized void release(String dimension, int chunkX, int chunkZ) {
    String key = key(dimension, chunkX, chunkZ);
    Integer count = tickets.get(key);
    if (count == null) {
      return;
    }
    if (count.intValue() <= 1) {
      tickets.remove(key);
    } else {
      tickets.put(key, Integer.valueOf(count.intValue() - 1));
    }
  }

  public synchronized int ticketCount(String dimension, int chunkX, int chunkZ) {
    Integer count = tickets.get(key(dimension, chunkX, chunkZ));
    return count == null ? 0 : count.intValue();
  }

  public synchronized int totalTickets() {
    int total = 0;
    for (Integer value : tickets.values()) {
      total += value.intValue();
    }
    return total;
  }

  private static String key(String dimension, int chunkX, int chunkZ) {
    return (dimension == null ? "unknown" : dimension) + ":" + chunkX + "," + chunkZ;
  }
}
