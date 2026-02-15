# 2025-12-08 Milestone 1

1. [x] 初始化 communication 设计文档与协议结构，覆盖 channel 常量、JSON 规范、服务接口。
2. [x] 在 `common` 模块内落地 `protocol`/`service`/`transport` 基础代码，为 Fabric/Forge 复用打桩。
3. [x] 输出 `docs/Channel API.md`，面向 Bukkit 插件描述 channel 前缀、请求/响应结构、示例、错误码。
4. [x] 执行 `./gradlew buildAllTargets` 验证多版本 mod 编译通过，并记录潜在告警。

# 2025-12-08 Milestone 2

- [x] Fabric 1.18.2 / 1.20.1 插件消息通道：注册 `hydroline:beacon_provider`、缓存 `MinecraftServer`、绑定 `MtrQueryGateway`。
- [x] Forge 1.18.2 / 1.20.1 插件消息通道：Netty pipeline 拦截 `ServerboundCustomPayloadPacket` 并回写 `ClientboundCustomPayloadPacket`。
- [x] Fabric 1.16.5 插件消息通道：沿用 v1 Channel 协议，使用 `Identifier` + `ServerPlayNetworking` 对接 1.16 API。
- [x] Forge 1.16.5 插件消息通道：登录事件注入 Netty handler，兼容 `CCustomPayloadPacket`/`SCustomPayloadPlayPacket`。
- [ ] **mtr:list_network_overview** — 背景：网站首页需要展示服务器里所有线路、车厂、收费区等概览，且与 Minecraft 解耦。Forge Provider 负责跨维度聚合 `routes`/`stations`/`depots`/`fareAreas`，输出 `dimensions[] -> { routes[], depots[], fareAreas[] }` 的结构，供外部一次性渲染。
- [ ] **mtr:get_route_detail** — 背景：玩家会点进单条线路查看节点信息。该 action 要根据 `dimension + routeId` 给出线路名、颜色、站台序列以及 node 级拓扑：通过 `Route.platformIds`、`DataCache.platformIdToStation` 绑定站点，再结合 `RailwayDataRouteFinderModule` 或 `rails` Map 提供节点坐标、`RailType`、是否为站台区段等标记。
- [ ] **mtr:list_depots** — 背景：外部页面需要展示各车厂的发车计划与关联线路。接口需列出所有 `Depot` 的 `routeIds`、`platformTimes`、`departures`、`useRealTime`/`repeatInfinitely` 并计算 `nextDepartureMillis`，同时附带引用到的线路名称方便展示。
- [ ] **mtr:list_fare_areas** — 背景：网站将收费区/站点区域画在地图上，需要快速获取 `stationId`、`name`、`zone`、`corner1/2`、换乘线路等信息。此 action 聚合 `Station` + `stationIdToConnectingStations`，输出可直接给 Leaflet 使用的多边形和元数据。
- [ ] **mtr:list_nodes_paginated** — 背景：外部地图还要绘制所有轨道节点，但 Forge Provider 只负责读 MTR 数据，批量汇总交给 Bukkit。设计一个支持 `dimension + cursor/limit` 的分页接口，返回 node 坐标、`RailType`、是否属于站台/停车轨道等属性，供 Bukkit 逐次拉取后集中推送给网站。
- [ ] **mtr:get_station_timetable** — 背景：用户需要查看某站或某站台的实时时刻表。封装 `RailwayData.getSchedulesForStation` / `getSchedulesAtPlatform` 并叠加 `getTrainDelays()`，返回每条记录的 `arrivalMillis`、`routeId`、`trainCars`、延误信息等，满足前端列表展示。

## 测试计划

- [x] `./gradlew :fabric-1.18.2:compileJava :fabric-1.20.1:compileJava :forge-1.18.2:compileJava :forge-1.20.1:compileJava`
- [x] `./gradlew :fabric-1.16.5:compileJava :forge-1.16.5:compileJava`
