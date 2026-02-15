package cn.hydcraft.hydronyasama.mtr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Simple time-based cache around snapshot collection to avoid hammering the Minecraft server
 * thread.
 */
public final class MtrSnapshotCache {
  private final Supplier<List<MtrDimensionSnapshot>> loader;
  private final long ttlMillis;

  private volatile long expiresAt;
  private volatile List<MtrDimensionSnapshot> cachedSnapshots = Collections.emptyList();

  public MtrSnapshotCache(Supplier<List<MtrDimensionSnapshot>> loader, long ttlMillis) {
    this.loader = Objects.requireNonNull(loader, "loader");
    this.ttlMillis = ttlMillis;
  }

  public List<MtrDimensionSnapshot> get() {
    long now = System.currentTimeMillis();
    List<MtrDimensionSnapshot> current = cachedSnapshots;
    if (now < expiresAt && current != null) {
      return current;
    }
    synchronized (this) {
      now = System.currentTimeMillis();
      if (now < expiresAt && cachedSnapshots != null) {
        return cachedSnapshots;
      }
      List<MtrDimensionSnapshot> loaded = loader.get();
      if (loaded == null) {
        loaded = Collections.emptyList();
      } else if (!(loaded instanceof ArrayList)) {
        loaded = new ArrayList<>(loaded);
      }
      cachedSnapshots = Collections.unmodifiableList(loaded);
      expiresAt = now + ttlMillis;
      return cachedSnapshots;
    }
  }

  public void invalidate() {
    synchronized (this) {
      cachedSnapshots = Collections.emptyList();
      expiresAt = 0L;
    }
  }
}
