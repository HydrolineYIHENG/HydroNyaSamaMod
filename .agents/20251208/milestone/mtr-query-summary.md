# MTR Query Gateway Summary

Date: 2025-12-08

## Completed Action Coverage

- `mtr:list_network_overview`: Loader gateways aggregate snapshot data into dimension overviews via `MtrDataMapper.buildNetworkOverview`, returning routes, depots, and fare areas per dimension.
- `mtr:get_route_detail`: Per-dimension snapshots map route metadata plus ordered platform nodes, enabling the frontend to render topology details from the `RouteDetail` payload.
- `mtr:list_depots`: Depots are serialized with departure plans, repeat/real-time flags, cruising altitude, and computed `nextDepartureMillis` for operational dashboards.
- `mtr:list_fare_areas`: Each station exposes zone/bounds/interchange route metadata, ready for Leaflet overlays or other GIS tooling.
- `mtr:list_nodes_paginated`: Large rail graphs stream through cursor-based pagination, capping responses to 2048 nodes per request for Bukkit aggregation.
- `mtr:get_station_timetable`: Station or platform-specific schedules merge `RailwayData` schedule APIs with delay snapshots to feed the website timetable view.

## Implementation Notes

1. **Unified loader gateways** (`FabricMtrQueryGateway`, `ForgeMtrQueryGateway` across 1.16.5 / 1.18.2 / 1.20.1) now refresh live snapshots before delegating to `MtrDataMapper`, ensuring every action has consistent data regardless of loader.
2. **`MtrRailwayDataAccess` helper** reflects `RailwayData#getInstance` to avoid depending on obfuscated Minecraft classes, unlocking compilation across all slim jars.
3. **`MtrDataMapper` + `MtrJsonWriter`** already expose the DTOs consumed by each action handler (`MtrListNetworkOverviewActionHandler` … `MtrGetStationTimetableActionHandler`), so no additional protocol glue was required beyond ensuring real data flows.

## Verification

- `./gradlew :fabric-1.20.1:compileJava :fabric-1.18.2:compileJava :fabric-1.16.5:compileJava :forge-1.20.1:compileJava :forge-1.18.2:compileJava :forge-1.16.5:compileJava`

All targeted Fabric/Forge variants compile successfully with the new gateway + action wiring.
