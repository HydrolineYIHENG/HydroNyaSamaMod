package cn.hydcraft.hydronyasama.service;

import cn.hydcraft.hydronyasama.protocol.BeaconMessage;
import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import cn.hydcraft.hydronyasama.transport.TransportContext;

/** Strategy per action so loader code can register integrations (MTR, Create, etc.). */
public interface BeaconActionHandler {
  String action();

  BeaconResponse handle(BeaconMessage message, TransportContext context);
}
