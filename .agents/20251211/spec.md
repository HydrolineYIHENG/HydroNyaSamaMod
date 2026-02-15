# 研究记录（2025-12-11）

## RailwayData 就是我们要的数据源

- `mtr.data.RailwayData`（见 `libs/mtr3/MTR-forge-1.18.2-3.2.2-hotfix-1-slim.jar`）持有 `stations`、`platforms`、`routes`、`depots`、`sidings`、`signalBlocks`、`rails` 等集合，所需全部拓扑/动态信息都能从这个类及其子集提取，完全在内存里，所以不必再直接扫 `world/mtr`；
- `RailwayData.writeMessagePackDataset` 会把每个 `SerializedDataBase`（如 `Station`/`Platform`/`Route`/`Depot`）的字段按 `id` 写入 `MessagePack`，然后写到对应的数组里，因此快照里既有 `stations` 数组、又有 `6840141479544014000` 这样的数字键。客户端可任选任意一处读取字段；
- `Pos` 所有的坐标为整数 block 位置，对应 `AreaBase.corner1/corner2` 与 `SavedRailBase` 里的 `BlockPos`，平台两端 `pos_1` / `pos_2` 直接就是 `BlockPos#asLong()` 编码，搭配 `BlockPos.of(long)` 或 `BlockPosEncoding` 可还原；
- `signalBlocks` 里每个 block 也包含与 `rails`（`Map<BlockPos, Map<BlockPos, Rail>>`）的关联，可以实时重排轨道节点，`nodes` 并不单独存储，只要连接这两张表即可重建轨迹；
- 由于 `RailwayData` 提供的 `routes.platformIds` 本身就是 `routeStationOrder`（MTR 中由 `RailwayDataRouteFinderModule` 维护的顺序），单独去解析 `routeStationOrder` 这种缓存就多余。

## 与 world/mtr 文件的关系

- `world/mtr/<dimension>` 下的 MessagePack、logs、rails 目录等，本质是 `RailwayData` 的持久化快照；我们的 Provider 只需要通过 `RailwayData` 读实时状态后再序列化，不需硬读世界存档，降低重算风险；
- `signal-blocks` 目录可以解出每个 Block 所属路段，便于 Leaflet 精细打点；它与 `RailwayData.signalBlocks` 结构一致（由 `SignalBlock` 记录 `railType`、`platformSegment`、`nodes` 列表），`signal-blocks` 内容即为 MTR 写出的 MessagePack 反序列化结果，足够用于精细地铁图；
- 如果需要离线分析，Bukkit 端仍然可以周期性扫描 `world/mtr`；Provider 只保留动态接口（实时列车位置、发车信息），静态结构由 Bukkit 缓存/对比，避免重复扫描。

## 快照数据量估算

- 目前 `tests/output/mtr_railway_snapshot_minecraft_overworld.msgpack` 大小约 3.5 MB，可见单维度的 `RailwayData` 在 MessagePack 形式下含有 rails、signalBlocks、stations、platforms、routes、depots 等字段，参数量远超过只包含 station/platform/route 的 JSON；
- 这个体积中，`rails` 是主要的膨胀项：`RailwayData` 维护 `Map<BlockPos, Map<BlockPos, Rail>>`，每条 rail 还包含 node 坐标（`RailwayDataRouteFinderModule` 访问）和 `signalBlocks`，决定了我们要传输的几何复杂度；
- 由于 Provider 只输出 Base64 的 MessagePack，而客户端可通过解码后缓存数据，实际流水量可控制在单维度几 MB，`last_deployed` 可用于判断是否需要重新请求。

## 结论与后续

- 既然 `RailwayData` 自带完整节点结构（rails + signalBlocks），我们可以放心把 Provider 里剩余的 `mtr` 模块仅用作读取 `RailwayData`、序列化 MessagePack 并返回，Beacon/前端负责还原与缓存；
- `.agents/20251211/todo.md` 里我会把上述工作拆成具体步骤，保持原有基础设施（Netty、config、tests）不动，只替换 Action。
