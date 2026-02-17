package cn.hydcraft.hydronyasama.core.train;

/** Train control facade used by loader commands and future network integration. */
public final class TrainControlService {
  private static final TrainControlService INSTANCE = new TrainControlService();

  private final TrainController controller = new TrainController();
  private final TrainControlServerHandler serverHandler = new TrainControlServerHandler(controller);
  private final TrainControlClientHandler clientHandler = new TrainControlClientHandler();

  private TrainControlService() {}

  public static TrainControlService getInstance() {
    return INSTANCE;
  }

  public synchronized TrainControlState applyControl(
      String trainId, int direction, int power, int brakeResistance, double velocity) {
    TrainPacket request = TrainPacket.control(trainId, direction, power, brakeResistance, velocity);
    TrainPacket wireRequest = TrainPacket.decode(request.encode());
    TrainPacket serverSync = serverHandler.handle(wireRequest);
    TrainPacket wireSync = TrainPacket.decode(serverSync.encode());
    clientHandler.handle(wireSync);
    return controller.snapshot(trainId);
  }

  public synchronized TrainControlState tick(
      String trainId,
      double mass,
      double friction,
      double tractionPower,
      double brakeStrength,
      double inductance,
      double resistance,
      double dt) {
    TrainControlState state =
        controller.tick(
            trainId, mass, friction, tractionPower, brakeStrength, inductance, resistance, dt);
    TrainPacket sync =
        TrainPacket.sync(
            trainId,
            state.getDirection(),
            state.getPower(),
            state.getBrakeResistance(),
            state.getVelocity());
    clientHandler.handle(TrainPacket.decode(sync.encode()));
    return state;
  }

  public synchronized TrainControlState snapshotServer(String trainId) {
    return controller.snapshot(trainId);
  }

  public synchronized TrainControlState snapshotClient(String trainId) {
    return clientHandler.snapshot(trainId);
  }

  public synchronized void reset() {
    controller.reset();
  }
}
