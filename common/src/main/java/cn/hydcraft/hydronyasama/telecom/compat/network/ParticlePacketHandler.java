package cn.hydcraft.hydronyasama.telecom.compat.network;

/** Compatibility packet handler delegating actual particle spawn to loader/runtime callback. */
public final class ParticlePacketHandler {
  public interface Emitter {
    void emit(ParticlePacket packet);
  }

  private ParticlePacketHandler() {}

  public static void handle(ParticlePacket packet, Emitter emitter) {
    if (packet == null || emitter == null) {
      return;
    }
    emitter.emit(packet);
  }
}
