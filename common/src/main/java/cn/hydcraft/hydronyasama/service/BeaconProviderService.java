package cn.hydcraft.hydronyasama.service;

import cn.hydcraft.hydronyasama.protocol.BeaconMessage;
import cn.hydcraft.hydronyasama.protocol.BeaconResponse;
import cn.hydcraft.hydronyasama.transport.TransportContext;

/**
 * Minimal API exposed to loader-specific entrypoints, decoupling Bukkit traffic from mod internals.
 */
public interface BeaconProviderService {
  BeaconResponse handle(BeaconMessage request, TransportContext context);
}
