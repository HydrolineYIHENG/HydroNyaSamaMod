package cn.hydcraft.hydronyasama.core.train;

/** Server-side compatibility handler for train control packets. */
public final class TrainControlServerHandler {
  private final TrainController trainController;

  public TrainControlServerHandler(TrainController trainController) {
    this.trainController = trainController;
  }

  public TrainPacket handle(TrainPacket packet) {
    if (packet.type() != TrainPacket.Type.CONTROL) {
      return packet;
    }
    TrainControlState state = trainController.apply(packet);
    return TrainPacket.sync(
        packet.trainId(),
        state.getDirection(),
        state.getPower(),
        state.getBrakeResistance(),
        state.getVelocity());
  }
}
