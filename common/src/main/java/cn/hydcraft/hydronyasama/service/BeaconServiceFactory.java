package cn.hydcraft.hydronyasama.service;

import cn.hydcraft.hydronyasama.service.create.CreateGetNetworkActionHandler;
import cn.hydcraft.hydronyasama.service.create.CreateGetRealtimeActionHandler;
import cn.hydcraft.hydronyasama.service.mtr.MtrGetAllStationSchedulesActionHandler;
import cn.hydcraft.hydronyasama.service.mtr.MtrGetDepotTrainsActionHandler;
import cn.hydcraft.hydronyasama.service.mtr.MtrGetRailwaySnapshotActionHandler;
import cn.hydcraft.hydronyasama.service.mtr.MtrGetRouteTrainsActionHandler;
import cn.hydcraft.hydronyasama.service.mtr.MtrGetStationScheduleActionHandler;
import java.util.Arrays;

/** Factory helpers to keep loader entrypoints concise. */
public final class BeaconServiceFactory {
  private BeaconServiceFactory() {}

  public static DefaultBeaconProviderService createDefault() {
    return new DefaultBeaconProviderService(
        Arrays.asList(
            new PingActionHandler(),
            new MtrGetRailwaySnapshotActionHandler(),
            new MtrGetRouteTrainsActionHandler(),
            new MtrGetStationScheduleActionHandler(),
            new MtrGetAllStationSchedulesActionHandler(),
            new MtrGetDepotTrainsActionHandler(),
            new CreateGetNetworkActionHandler(),
            new CreateGetRealtimeActionHandler()));
  }
}
