package cn.hydcraft.hydronyasama.forge.create;

import cn.hydcraft.hydronyasama.BeaconProviderMod;
import cn.hydcraft.hydronyasama.create.CreateDatabase;
import cn.hydcraft.hydronyasama.create.CreateNetworkSnapshot;
import cn.hydcraft.hydronyasama.create.CreateQueryGateway;
import cn.hydcraft.hydronyasama.create.CreateRealtimeSnapshot;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import net.minecraft.server.MinecraftServer;

public final class ForgeCreateQueryGateway implements CreateQueryGateway {
  private final Supplier<MinecraftServer> serverSupplier;
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicBoolean ready = new AtomicBoolean(false);
  private CreateDatabase database;
  private CreateRealtimeChannel realtimeChannel;
  private CreateStaticSnapshotService staticSnapshotService;

  public ForgeCreateQueryGateway(Supplier<MinecraftServer> serverSupplier) {
    this.serverSupplier = serverSupplier;
  }

  public void start(Path configDir) {
    if (!started.compareAndSet(false, true)) {
      return;
    }
    Path dbPath = configDir.resolve("HydroNyaSama").resolve("cache.db");
    this.database = new CreateDatabase(dbPath);
    try {
      database.initialize();
    } catch (Exception ex) {
      BeaconProviderMod.LOGGER.warn("Failed to initialize Create cache database", ex);
    }
    this.realtimeChannel =
        new CreateRealtimeChannel(serverSupplier, snapshot -> ready.set(snapshot != null));
    this.staticSnapshotService = new CreateStaticSnapshotService(serverSupplier, database);
    realtimeChannel.start();
    staticSnapshotService.start();
  }

  public void stop() {
    if (!started.compareAndSet(true, false)) {
      return;
    }
    if (realtimeChannel != null) {
      realtimeChannel.stop();
      realtimeChannel = null;
    }
    if (staticSnapshotService != null) {
      staticSnapshotService.stop();
      staticSnapshotService = null;
    }
    if (database != null) {
      try {
        database.close();
      } catch (Exception ex) {
        BeaconProviderMod.LOGGER.warn("Failed to close Create cache database", ex);
      }
      database = null;
    }
    ready.set(false);
  }

  @Override
  public boolean isReady() {
    return started.get() && ready.get();
  }

  @Override
  public Optional<CreateNetworkSnapshot> fetchNetworkSnapshot(
      String graphId, boolean includePolylines) {
    CreateDatabase current = database;
    if (current == null) {
      return Optional.empty();
    }
    if (graphId != null && !current.hasGraph(graphId)) {
      return Optional.empty();
    }
    return Optional.of(current.queryNetworkSnapshot(graphId, includePolylines));
  }

  @Override
  public CreateRealtimeSnapshot fetchRealtimeSnapshot() {
    CreateRealtimeChannel channel = realtimeChannel;
    if (channel == null) {
      return CreateRealtimeSnapshot.empty();
    }
    return channel.getSnapshot();
  }
}
