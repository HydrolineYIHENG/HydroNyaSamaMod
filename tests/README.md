# Beacon Provider Action Tests

这个目录包含一个最简的 Node.js 测试脚本，用来通过 Netty Gateway 触发 `mtr:get_railway_snapshot`，并将 Provider 返回的快照写入 `tests/output` 以供调试 / 与 Bukkit 数据对比。

## 准备

1. 确保 Beacon Provider 运行（Fabric/Forge 端正在监听 `config/hydroline/HydroNyaSama.json` 中配置的 `listenAddress`/`listenPort`，默认 `127.0.0.1:28545`）。
2. 安装 Node.js ≥ 18（无需额外依赖，`tests/package.json` 目前没有依赖库）。
3. 可选：在 `tests/.env` 里覆盖连接参数，例如：
   ```ini
   PROVIDER_HOST=127.0.0.1
   PROVIDER_PORT=28545
   PROVIDER_TOKEN=change-me
   PROVIDER_MTR_DIMENSION=minecraft:overworld
   OUTPUT_DIR=./output
   REQUEST_TIMEOUT_MS=15000
   PROVIDER_MTR_ROUTE_ID=0
   PROVIDER_MTR_STATION_ID=123
   PROVIDER_MTR_PLATFORM_ID=456
   PROVIDER_MTR_DEPOT_ID=0
   ```

## 执行

```bash
cd tests
pnpm test:actions
```

脚本会：

- 清空 `tests/output`；
- 握手并调用 `mtr:get_railway_snapshot`（可选 `dimension` 参数）；
- 将原始响应写入 `mtr_railway_snapshot_<dimension>.json`；
- 解析 `payload` 字段中的 Base64 messagepack，写入 `mtr_railway_snapshot_<dimension>.msgpack`；
- 使用 `msgpack-lite` 解码 `.msgpack` 内容，并写出 `mtr_railway_snapshot_<dimension>.json`，方便直接比对结构；
- 保留 `beacon:ping.json` 便于比对。
- 调用 `mtr:get_route_trains` 并写出 `mtr_route_trains_<dimension>_route_<routeId|all>.json`；
- 如果设置了 `PROVIDER_MTR_STATION_ID`，再请求 `mtr:get_station_schedule`（可选 `platformId`）并写入 `mtr_station_schedule_<dimension>_station_<stationId>.json`；
- 请求 `mtr:get_depot_trains`（可选 `depotId`）并保存到 `mtr_depot_trains_<dimension>_<depotId|all>.json`，方便查看每个 depot 的 departures + train 状态。
- 调用 `mtr:get_route_trains` 并写出 `mtr_route_trains_<dimension>_route_<routeId|all>.json`；
- 如果设置了 `PROVIDER_MTR_STATION_ID`，再请求 `mtr:get_station_schedule`（可选 `platformId`）并写入 `mtr_station_schedule_<dimension>_station_<stationId>.json`。

你可直接用 `pnpm test:actions` 生成的 `.msgpack` 输入你自己的解析器，进一步还原 `stations`/`routes`/`rails` 等结构。
