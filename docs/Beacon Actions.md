# Beacon Actions

Beacon Provider 只保留最小的 `action` 集合，专注于提供当前 Minecraft 世界的 MTR + Create 数据快照：静态结构数据由 Bukkit 端缓存或由 Provider 侧 SQLite 缓存，Provider 只需要返回动态的状态，供前端/Beacon 进行进一步处理。

## 0. 统一响应结构（基于 tests/output）

tests/output 的返回结果结构如下：

- 顶层：
  - `timestamp`：ISO 8601 字符串（日志/采集层写入的时间）。
  - `data`：Beacon Provider 的实际响应体。
- `data`：
  - `protocolVersion`：协议版本（int）。
  - `requestId`：请求 ID（string）。
  - `result`：结果码（string），枚举：`OK` / `BUSY` / `INVALID_ACTION` / `INVALID_PAYLOAD` / `NOT_READY` / `ERROR`。
  - `message`：错误/提示信息（string，通常为空）。
  - `payload`：action 的业务返回体（object）。

> 说明：Provider 直接输出的核心响应体就是 `data` 部分；tests/output 额外包了一层 `timestamp` + `data` 用于记录。

## 1. 可用 Action 一览

| Action 名称                     | 说明                                                                  | 请求 `payload`                                           | 响应 `payload`                                                                                                          |
| ------------------------------- | --------------------------------------------------------------------- | -------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| `beacon:ping`                   | 验证 Gateway 通信，并测量往返延迟。                                   | 可选：`echo` (`string`)                                  | `echo`、`receivedAt`、`latencyMs`                                                                                       |
| `mtr:get_railway_snapshot`      | 返回一个或多个维度当前的 `RailwayData` 快照（MessagePack + Base64）。 | 可选：`dimension`（如 `minecraft:overworld`）            | `format`、`snapshots[]`                                                                                                 |
| `mtr:get_route_trains`          | 返回指定维度/线路上正在运行的列车列表。                               | 可选：`dimension`、`routeId`（不传则返回全部线路）       | `timestamp`、`dimension?`、`routeId?`、`trains[]`                                                                       |
| `mtr:get_depot_trains`          | 返回车厂（Depot）信息及其列车列表。                                   | 可选：`dimension`、`depotId`（不传则返回全部车厂）       | `timestamp`、`dimension?`、`depots[]`                                                                                   |
| `mtr:get_station_schedule`      | 查询某个车站（可选站台）的时刻表。                                    | 必需：`stationId`；可选：`dimension`、`platformId`       | `timestamp`、`stationId`、`dimension?`、`timetables[]`                                                                  |
| `mtr:get_all_station_schedules` | 扫描所有维度的 station/platform，返回每个平台的时刻表。               | 可选：`dimension`                                        | `timestamp`、`dimension?`、`note?`、`dimensions[]`                                                                      |
| `create:get_network`            | 返回 Create 轨道网络的静态结构数据（SQLite 缓存）。                   | 可选：`graphId`；可选：`includePolylines`（默认 `true`） | `timestamp`、`graphs[]`、`nodes[]`、`edges[]`、`edgePolylines?[]`、`stations[]`、`signalBoundaries[]`、`edgeSegments[]` |
| `create:get_realtime`           | 返回 Create 实时列车/区段占用信息（内存快照）。                       | 无                                                       | `timestamp`、`trains[]`、`groups[]`                                                                                     |

## 2. MTR 动作说明

### 2.1 `mtr:get_railway_snapshot`

请求 `payload`：

- `dimension`（string，可选）：限定维度；不传则返回所有已缓存维度。

响应 `payload`：

- `format`：固定为 `messagepack`。
- `snapshots[]`：每个维度一个快照对象：
  - `dimension`：维度标识（string）。
  - `format`：`messagepack`。
  - `timestamp`：序列化时间戳（ms）。
  - `length`：原始 MessagePack 字节数（int）。
  - `payloadChunks`：分片后的 Base64 数据：
    - `encoding`：`base64`。
    - `decodedLength`：解码后长度（int）。
    - `chunkCount`：分片数量（int）。
    - `chunkSize`：分片大小（int）。
    - `chunks[]`：`{ index, data }`，按 `index` 升序拼接 `data` 后再 Base64 解码。

### 2.2 `mtr:get_route_trains`

请求 `payload`：

- `dimension`（string，可选）。
- `routeId`（long，可选；不传则返回全部线路）。

响应 `payload`：

- `timestamp`：毫秒时间戳。
- `dimension`（string，可选）：仅当请求中传入时返回。
- `routeId`（long，可选）：仅当请求中传入时返回。
- `trains[]`：列车列表（TrainStatus）：
  - `trainUuid`（string，可选）
  - `trainId`（string，可选）
  - `dimension`（string）
  - `routeId`（long）
  - `depotId`（long，可选）
  - `transportMode`（string）
  - `currentStationId`（long，可选）
  - `nextStationId`（long，可选）
  - `delayMillis`（long，可选）
  - `railId`（long，可选）
  - `segmentCategory`（int）
  - `progress`（double）
  - `node`（object，可选）：
    - `x` / `y` / `z`（double）
    - `railType`（string）
    - `platformSegment`（boolean）
    - `stationId`（long，可选）

### 2.3 `mtr:get_depot_trains`

请求 `payload`：

- `dimension`（string，可选）。
- `depotId`（long，可选）。

响应 `payload`：

- `timestamp`：毫秒时间戳。
- `dimension`（string，可选）。
- `depots[]`：车厂列表：
  - `depotId`（long）
  - `name`（string）
  - `departures[]`（int 数组，单位 ms）
  - `routeIds[]`（long 数组）
  - `useRealTime`（boolean）
  - `repeatInfinitely`（boolean）
  - `cruisingAltitude`（int）
  - `nextDepartureMillis`（long，可选）
  - `trains[]`：列车列表，字段同 `mtr:get_route_trains` 的 `trains[]`

### 2.4 `mtr:get_station_schedule`

请求 `payload`：

- `stationId`（long，必需）
- `dimension`（string，可选）
- `platformId`（long，可选）

响应 `payload`：

- `timestamp`：毫秒时间戳。
- `stationId`（long）
- `dimension`（string，可选）
- `timetables[]`：每个维度一个条目：
  - `dimension`（string）
  - `platforms[]`：
    - `platformId`（long）
    - `platformName`（string，可选）
    - `entries[]`：
      - `routeId`（long）
      - `routeName`（string，可选）
      - `name`（string，可选，等同于 routeName）
      - `destination`（string，可选）
      - `circular`（string，可选）
      - `route`（string，可选，线路标签）
      - `color`（int，可选）
      - `arrivalMillis`（long）
      - `trainCars`（int）
      - `currentStationIndex`（int）

### 2.5 `mtr:get_all_station_schedules`

请求 `payload`：

- `dimension`（string，可选）

响应 `payload`：

- `timestamp`：毫秒时间戳。
- `dimension`（string，可选）
- `note`（string，可选，仅在无时刻表时返回提示）
- `dimensions[]`：
  - `dimension`（string）
  - `stations[]`：
    - `stationId`（long）
    - `stationName`（string）
    - `platforms[]`：
      - `platformId`（long）
      - `platformName`（string，可选）
      - `entries[]`：
        - `routeId`（long）
        - `routeName`（string，可选）
        - `destination`（string，可选）
        - `circular`（string，可选）
        - `route`（string，可选）
        - `color`（int，可选）
        - `arrivalMillis`（long）
        - `trainCars`（int）
        - `currentStationIndex`（int）
        - `delayMillis`（long，可选）

## 3. Create 动作说明（1.20.1）

### 3.1 `create:get_network`

请求 `payload`：

- `graphId`（string，可选）：为空则返回全部网络。
- `includePolylines`（boolean，可选，默认 `true`）。

响应 `payload`：

- `timestamp`：毫秒时间戳。
- `graphs[]`：
  - `graphId`（string）
  - `checksum`（int）
  - `color`（int）
  - `updatedAt`（long）
- `nodes[]`：
  - `graphId`（string）
  - `netId`（int）
  - `dimension`（string）
  - `x` / `y` / `z`（double）
  - `normal`（数组 `[x,y,z]`）
  - `yOffsetPixels`（int）
- `edges[]`：
  - `edgeId`（string）
  - `graphId`（string）
  - `node1NetId` / `node2NetId`（int）
  - `isTurn`（boolean）
  - `isPortal`（boolean）
  - `length`（double）
  - `materialId`（string，可选）
- `edgePolylines[]`（仅 `includePolylines=true`）：
  - `edgeId`（string）
  - `points`（数组，点为 `[x,y,z]`）
- `stations[]`：
  - `stationId`（string）
  - `graphId`（string）
  - `edgeId`（string）
  - `position`（double）
  - `name`（string，可选）
  - `dimension`（string）
  - `x` / `y` / `z`（double）
- `signalBoundaries[]`：
  - `boundaryId`（string）
  - `graphId`（string）
  - `edgeId`（string）
  - `position`（double）
  - `groupIdPrimary`（string，可选）
  - `groupIdSecondary`（string，可选）
  - `dimension`（string）
  - `x` / `y` / `z`（double）
- `edgeSegments[]`：
  - `segmentId`（string）
  - `edgeId`（string）
  - `startPos` / `endPos`（double）
  - `groupId`（string，可选）

### 3.2 `create:get_realtime`

请求 `payload`：无。

响应 `payload`：

- `timestamp`：毫秒时间戳。
- `trains[]`：
  - `trainId`（string）
  - `name`（string）
  - `iconId`（string，可选）
  - `mapColorIndex`（int）
  - `status`（string，可选，Create TrainStatus 的 toString）
  - `speed` / `targetSpeed` / `throttle`（double）
  - `derailed`（boolean）
  - `graphId`（string，可选）
  - `currentStationId`（string，可选）
  - `scheduleTitle`（string，可选）
  - `scheduleEntry`（int，可选）
  - `scheduleState`（string，可选）
  - `schedulePaused` / `scheduleCompleted` / `scheduleAuto`（boolean）
  - `positions[]`：`{ dimension, x, y, z }`
  - `carriages[]`：
    - `id`（int）
    - `bogeySpacing`（int）
    - `leading` / `trailing`（object，可选）：
      - `edgeId`（string，可选）
      - `node1NetId` / `node2NetId`（int）
      - `position`（double）
      - `dimension`（string，可选）
      - `x` / `y` / `z`（double）
    - `leadingBogey` / `trailingBogey`（object，可选）：
      - `styleId`（string，可选）
      - `size`（string，可选）
      - `upsideDown`（boolean）
- `groups[]`：
  - `groupId`（string）
  - `color`（string，可选）
  - `reservedBoundaryId`（string，可选）
  - `trainIds[]`（string 数组）

## 4. 站点时刻表请求限流

- `mtr:get_station_schedule` 与 `mtr:get_all_station_schedules` 共用一个串行请求队列，Provider 后台只运行一个线程。
- 每次处理之间至少等待 `beacon.scheduleRateLimitMs` 毫秒（默认 400ms），队列最大等待请求数为 64。
- 请求在队列耗尽或等待超时（默认 `beacon.scheduleRequestTimeoutMs=30000`）时会返回 `ResultCode.BUSY`，客户端应当捕捉并退避重试。
- 可通过 `-Dbeacon.scheduleRateLimitMs=500` 或 `-Dbeacon.scheduleRequestTimeoutMs=60000` 调整限流与超时。

## 5. 示例返回体（节选）

> 以下示例均来自 `tests/output`，为节省篇幅仅保留部分数组元素。

### 5.1 `beacon:ping`

```json
{
  "timestamp": "2026-01-26T22:07:12.917Z",
  "data": {
    "protocolVersion": 1,
    "requestId": "juv00lqrr0gu",
    "result": "OK",
    "message": "",
    "payload": {
      "echo": "tests",
      "receivedAt": 1769465233700,
      "latencyMs": 0
    }
  }
}
```

### 5.2 `create:get_network`

```json
{
  "timestamp": "2026-01-26T22:07:14.698Z",
  "data": {
    "protocolVersion": 1,
    "requestId": "yu9rs3zqlx7k",
    "result": "OK",
    "message": "",
    "payload": {
      "timestamp": 1769465235582,
      "graphs": [
        {
          "graphId": "05bddc62-bf49-4cbb-b54a-fb4d09de432b",
          "checksum": 344503,
          "color": -16711829,
          "updatedAt": 1769464581748
        }
      ],
      "nodes": [
        {
          "graphId": "05bddc62-bf49-4cbb-b54a-fb4d09de432b",
          "netId": 5571,
          "dimension": "minecraft:overworld",
          "x": 6352,
          "y": 49,
          "z": 2408,
          "normal": [0, 1, 0],
          "yOffsetPixels": 0
        }
      ],
      "edges": [
        {
          "edgeId": "f9563a12-d527-32a0-be81-c9b98fb3671c",
          "graphId": "05bddc62-bf49-4cbb-b54a-fb4d09de432b",
          "node1NetId": 5550,
          "node2NetId": 5558,
          "isTurn": false,
          "isPortal": false,
          "length": 16,
          "materialId": "create:andesite"
        }
      ],
      "edgePolylines": [
        {
          "edgeId": "00108e23-eae7-325a-bfbf-67bab2df05b8",
          "points": [
            [6577.5, 49, 2048],
            [6577.5, 49, 2047]
          ]
        }
      ],
      "stations": [],
      "signalBoundaries": [],
      "edgeSegments": [
        {
          "segmentId": "86664a82-dd50-337a-a67e-d8b0e253a14d:0",
          "edgeId": "86664a82-dd50-337a-a67e-d8b0e253a14d",
          "startPos": 0,
          "endPos": 22.627416997969522,
          "groupId": "05bddc62-bf49-4cbb-b54a-fb4d09de432b"
        }
      ]
    }
  }
}
```

### 5.3 `create:get_realtime`

```json
{
  "timestamp": "2026-01-26T22:07:15.916Z",
  "data": {
    "protocolVersion": 1,
    "requestId": "0gd9rslw8aoz",
    "result": "OK",
    "message": "",
    "payload": {
      "timestamp": 1769465236769,
      "trains": [
        {
          "trainId": "2e3f3c29-2637-430b-94a2-c6e4cc60facc",
          "name": "GX01",
          "iconId": "create:traditional",
          "mapColorIndex": 0,
          "status": "com.simibubi.create.content.trains.entity.TrainStatus@1a57d184",
          "speed": 2.952499978709966,
          "targetSpeed": 2,
          "throttle": 0.8500000238418579,
          "derailed": false,
          "graphId": "4f93fcf7-f1b8-4ae3-8568-2c6dfdc31cc9",
          "scheduleTitle": "",
          "scheduleEntry": 4,
          "scheduleState": "IN_TRANSIT",
          "schedulePaused": false,
          "scheduleCompleted": false,
          "scheduleAuto": true,
          "positions": [
            {
              "dimension": "minecraft:overworld",
              "x": -2268,
              "y": 58,
              "z": 11208
            }
          ],
          "carriages": [
            {
              "id": 1,
              "bogeySpacing": 16,
              "leading": {
                "edgeId": "7ad48384-b02f-3774-bd12-6d2142729a8c",
                "node1NetId": 3809,
                "node2NetId": 3881,
                "position": 9.728974798074164,
                "dimension": "minecraft:overworld",
                "x": -2268.7473260317793,
                "y": 58.89490152990894,
                "z": 11208.500000515218
              },
              "trailing": {
                "edgeId": "3f484fdf-241d-3904-b775-52dcf98f2cc5",
                "node1NetId": 3826,
                "node2NetId": 3802,
                "position": 10.740562730234444,
                "dimension": "minecraft:overworld",
                "x": -2250.740562438965,
                "y": 59,
                "z": 11208.5
              },
              "leadingBogey": {
                "styleId": "create:standard",
                "size": "create:small",
                "upsideDown": false
              },
              "trailingBogey": {
                "styleId": "create:standard",
                "size": "create:small",
                "upsideDown": false
              }
            }
          ]
        }
      ],
      "groups": [
        {
          "groupId": "5fcc3216-827a-40b2-a1ae-c4004b6df9d2",
          "color": "BLUE",
          "trainIds": []
        }
      ]
    }
  }
}
```

### 5.4 `mtr:get_station_schedule`

```json
{
  "timestamp": "2026-01-26T22:07:13.589Z",
  "data": {
    "protocolVersion": 1,
    "requestId": "pqsbq523qpaj",
    "result": "OK",
    "message": "",
    "payload": {
      "timestamp": 1769465234412,
      "stationId": 3407626934243632000,
      "dimension": "minecraft:overworld",
      "timetables": [
        {
          "dimension": "minecraft:overworld",
          "platforms": [
            {
              "platformId": -1991389878067783000,
              "platformName": "JC",
              "entries": [
                {
                  "routeId": -1325051682189213400,
                  "routeName": "中央线|JC Line||西城-北安 各停",
                  "name": "中央线|JC Line||西城-北安 各停",
                  "destination": "北安|Bei'an",
                  "circular": "",
                  "route": "各停|Local",
                  "color": 16755390,
                  "arrivalMillis": 1769465261777,
                  "trainCars": 8,
                  "currentStationIndex": 6
                }
              ]
            }
          ]
        }
      ]
    }
  }
}
```
