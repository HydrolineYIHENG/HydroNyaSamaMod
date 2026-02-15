package cn.hydcraft.hydronyasama.transport;

import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import java.util.UUID;

/** Abstraction for sending responses back through the plugin channel. */
public interface ChannelMessenger {
  void reply(UUID playerUuid, BeaconResponse response);
}
