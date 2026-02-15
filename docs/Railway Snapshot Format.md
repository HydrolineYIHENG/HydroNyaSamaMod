# RailwayData 快照格式说明

`mtr:get_railway_snapshot` 返回的响应中，`payloadChunks` 按 `format: "messagepack"` 指定的 MessagePack 数据进行了 Base64 编码，并进一步拆成多个可控长度的 chunk：客户端需要按照 `payloadChunks.chunks` 数组里的 `index` 顺序把 `data` 字符串拼起来，再进行后续的 Base64 + MessagePack 解析。`payloadChunks` 的结构为：

- `encoding`：当前只支持 `base64`，指示 `data` 是经过 Base64 编码后的字符串；
- `decodedLength`：原始 MessagePack 字节数组的长度；
- `chunkCount` / `chunkSize`：用于验证和调试的 chunk 数量与每片最大字符长度；
- `chunks`：数组，元素为 `{ index, length, data }`，其中 `data` 就是当前 chunk 的 Base64 字符串片段。

根节点包含：

- `stations`、`platforms`、`routes`、`depots` 这四个数组，对应 `RailwayData.stations`、`RailwayData.platforms`、`RailwayData.routes`、`RailwayData.depots` 里保存的 `SerializedDataBase` 子类记录；
- `last_deployed` 保存了 `RailwayData` 的 `lastDeployedMillis`（毫秒），用于判断快照是否过期；
- 除了上述数组外，每个对象还会在根节点以其 `id`（long）作为 `MessagePack` key 重复一次（例如 `6840141479544014000`），这来自 `writeMessagePackDataset` 的 `Map` 写入，方便客户端直接按 `id` 查找而不必遍历数组；
- 由于 `RailwayData` 内部还包含 `rails`、`signalBlocks`、`sidings`、`lifts` 等集合，Provider 会返回这些集合对应的 `Map`（可在 `tests/output/*.json` 里看到序列化输出），但核心数据显示以 `stations` / `platforms` / `routes` / `depots` 为主，用于快速复刻世界拓扑。

解析顺序：

1. 按 `payloadChunks.chunks` 里 `index` 升序拼接所有 `data` 字符串片段，得到完整的、长度约为 `payloadChunks.chunkCount * chunkSize` 的 Base64 字符串。
2. Base64 解码拼接后的字符串 → 获得 MessagePack 原始字节；
2. 用 `MessagePack` 解码器将 bytes 转为对象（`msgpack-lite`、`msgpack-core` 均可，Beacon 脚本里使用 `msgpack-lite`）；
3. 如需做地图绘制，可借助 `BlockPos` 的 `asLong()` 编码（64-bit，见 `net.minecraft.core.BlockPos#asLong()` / `BlockPosEncoding`）来还原再转换成 x/y/z。

## 1. Stations（对应 `mtr.data.Station`）

| 字段 | 描述 | 单位 / 备注 |
| ---- | ---- | ----------- |
| `id` | `SerializedDataBase` 唯一 ID，根节点里也会以该值为 key 重复；`routeStationOrder` 等结构直接通过此值关联 | long |
| `transport_mode` | 与 `TransportMode` 枚举一致，指明是 `TRAIN`、`LIGHT_RAIL` 等 | string |
| `name` | 站点名称（常见格式 `中文|English`），来自 `NameColorDataBase#name` |  |
| `color` | 名称颜色，源自 `NameColorDataBase#color`，十进制整数 | int |
| `x_min` / `z_min`、`x_max` / `z_max` | 站点 `AreaBase.corner1`/`corner2` 的 Block 坐标，可直接用于 Leaflet 包围盒或多边形 | Block 坐标（整数） |
| `zone` | `Station.zone`，表示收费区编号 | int |
| `exits` | `Map<String, List<String>>`，键为出口名（如 `A1`），值为 `Station.serializeExit` 生成的位置信息，空对象表示没有专门出口 | 经 `BlockPos` 编码（字符串或 `[x,z]`） |

## 2. Platforms（对应 `mtr.data.Platform`）

| 字段 | 描述 | 单位 / 备注 |
| ---- | ---- | ----------- |
| `id`、`transport_mode`、`name`、`color` | 继承自 `NameColorDataBase`，分别表示平台 ID、运输模式、名称、颜色 | `id` 为 long，其他按类型 |
| `pos_1` / `pos_2` | 两端 `SavedRailBase` 的 `BlockPos`，MessagePack 中为 `BlockPos.asLong()` 编码，可通过 `BlockPos.of(long)` 还原 | long（BlockPos 编码） |
| `dwell_time` | `SavedRailBase.dwellTime`，列车在平台停留的时长，由 `Platform.setDwellTime` 控制 | Minecraft tick（50 ms） |
| `station_id` / `platform_segment` / `route_ids` / `depot_id` / `segment_category` 等 | 部分快照带的拓扑字段，用于将平台与站点、路线、车厂关联，来源于 `SavedRailBase` 与 `Platform` 运行时结构 | `route_ids` 为 long 列表，`segment_category` 例如 `PLATFORM` |
| 顺序说明 | `platforms` 数组顺序并非行车方向，实际路线顺序由 `routes.platform_ids` 决定 | 可结合 `routeStationOrder` 与 `signalBlocks` 还原 |

## 3. Routes（对应 `mtr.data.Route`）

| 字段 | 描述 | 单位 / 备注 |
| ---- | ---- | ----------- |
| `id`、`transport_mode`、`name`、`color` | 遵循 `NameColorDataBase` 的基础字段，用于跨集合关联 | `id` 为 long，其它按类型 |
| `platform_ids` | `Route.platformIds` 的有序列表，直接就是列车沿途经过的平台顺序，可视为 `routeStationOrder` | List<long> |
| `custom_destinations` | 每个平台（同 `platform_ids` 对应）用作广播目标的字符串，默认可为空 | List<string> |
| `route_type` | `RouteType` 枚举值（如 `NORMAL`、`HIGH_SPEED`），反映调度/行车好区分 | string |
| `is_light_rail_route` / `is_route_hidden` / `disable_next_station_announcements` | 布尔开关：控制轻轨子路线、隐藏运营线路以及是否播放下站广播 | boolean |
| `light_rail_route_number` | 形如 `T`、`C1303` 的编号，定位具体轻轨/高铁车次 | string |
| `circular_state` | `Route.CircularState`（`NONE`/`CLOCKWISE`/`COUNTERCLOCKWISE`），指出是否环线 | string |
| 拓扑还原 | `routes` 会结合 `signalBlocks` 与 `rails`（`Map<BlockPos, Map<BlockPos, Rail>>`）拼出 `nodes`，`Route.platform_ids` 本身即稳定的 `routeStationOrder` 表示 | 说明 |

## 4. Depots（对应 `mtr.data.Depot`）

| 字段 | 描述 | 单位 / 备注 |
| ---- | ---- | ----------- |
| `id`、`transport_mode`、`name`、`color` | 与 `NameColorDataBase` 统一，表示车厂 ID、模式、名称、颜色 | `id` 为 long |
| `x_min` / `z_min`、`x_max` / `z_max` | `AreaBase` corner 坐标，表示车厂占地区域 | Block 坐标 |
| `route_ids` | 可发车的路线 ID 集合，关联 `routes` | List<long> |
| `use_real_time` / `repeat_infinitely` | 是否启用实时发车/是否无限循环 | boolean |
| `cruising_altitude` | 列车飞行高度（块级） | Block 高度 |
| `frequencies`、`departures` | `Depot.MILLISECONDS_PER_DAY` 为单位，分别记录段落频率与发车时间偏移（从当天 00:00 起累加） | 毫秒 |
| `deploy_index` | `Depot.getNextDepartureMillis()` 所指的下一个排班索引 | int |
| `last_deployed` | `Depot.lastDeployedMillis`，表示最近一次实际部署的毫秒时间戳 | 毫秒 |
| `platformTimes` 等 | `Map<long, Map<BlockPos, Float>>` 等选填字段记录平台间耗时 | 说明 |

## 编码与转换补充

- `RailwayData` 所有位置字段（`x_min/z_min`、`pos_1/pos_2`、`x_max/z_max`）均以 Minecraft Block 坐标（整数）保存，可以通过 `net.minecraft.core.BlockPos#asLong()` / `BlockPos.of(long)` 互相转换，Leaflet 只需将 Block 坐标映射到地理投影；
- `RailwayData.writeMessagePackDataset` 会先写入 `id`→object 的 `Map`，再把对象追加到对应的 `stations` / `platforms` / `routes` / `depots` 数组，因此解码后的 JSON 会同时出现数组与大数字键（如 `6840141479544014000`）；这些重复的对象是同一份内容，客户端可任选一个用法；
- 拼接并解码后的 `payload` 中出现的 `signalBlocks`、`rails`、`sidings`、`lifts` 等集合可以拿来还原 `node` 信息：节点并非单独存储，而是由 `rails`（`Map<BlockPos, Map<BlockPos, Rail>>`） 与 `signalBlocks`（`Map<Long, SignalBlock>`）动态组合而成，`nodes` 可以通过 `MTR` 的辅助类（如 `Platform.getOrderedPositions`、`RailwayDataRouteFinderModule`）再现；
- 由于 Snapshot 里的数据量较大（单维度 MessagePack 约 3.5 MB，在 `tests/output/mtr_railway_snapshot_minecraft_overworld.msgpack` 可以看到），建议在客户端缓存解码结果，只在 `last_deployed` 变化时重新拉取。
