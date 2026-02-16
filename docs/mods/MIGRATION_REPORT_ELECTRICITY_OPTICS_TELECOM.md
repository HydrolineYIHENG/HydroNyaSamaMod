# NyaSamaElectricity + NyaSamaOptics + NyaSamaTelecom 迁移报告（阶段一，2026-02-15）

## 范围与目标
- 源模组：`.reference/NyaSamaElectricity`、`.reference/NyaSamaOptics`、`.reference/NyaSamaTelecom`（Forge 1.12.2）。
- 目标基线：并入当前 Architectury 多版本工程，覆盖 Fabric/Forge 的 1.16.5、1.18.2、1.20.1。
- 本阶段原则：优先“可编译、可注册、可进创造栏、可放置”，复杂逻辑按稳定性分级。
- 包名规范：新增迁移代码统一放入 `cn.hydcraft.hydronyasama.electricity.*`、`cn.hydcraft.hydronyasama.optics.*`、`cn.hydcraft.hydronyasama.telecom.*`。

## 成功迁移模块（可直接使用）
- Common 内容注册骨架：
  - `common/src/main/java/cn/hydcraft/hydronyasama/electricity/content/ElectricityContent.java`
  - `common/src/main/java/cn/hydcraft/hydronyasama/optics/content/OpticsContent.java`
  - `common/src/main/java/cn/hydcraft/hydronyasama/telecom/content/TelecomContent.java`
- 模组引导接入：
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/content/ModContent.java` 已接入三模块 `register(...)`。
- Forge 三版本注册列表接入（按新分组）：
  - `common/src/main/java/cn/hydcraft/hydronyasama/content/LegacyContentIds.java` 新增 Electricity/Optics/Telecom 的 block id 列表。
- 创造模式页签接入（6 目标全部覆盖）：
  - Fabric: `fabric-1.16.5/.../FabricContentRegistrar.java`、`fabric-1.18.2/.../FabricContentRegistrar.java`、`fabric-1.20.1/.../FabricContentRegistrar.java`
  - Forge: `forge-1.16.5/.../ForgeCreativeTabs.java`、`forge-1.18.2/.../ForgeCreativeTabs.java`、`forge-1.20.1/.../ForgeCreativeTabs.java`
  - Forge 内容分发：`forge-1.16.5/.../ForgeContentRegistry.java`、`forge-1.18.2/.../ForgeContentRegistry.java`、`forge-1.20.1/.../ForgeContentRegistry.java`
- 语言键补充（新页签）：
  - `common/src/main/resources/assets/hydronyasama/lang/en_us.json`
- 占位资源兜底（避免紫黑）：
  - 为三模块新增方块批量生成了 `blockstates + models/block + models/item` 占位资源（统一铁块外观），确保在未迁完高精模型前可正常显示与放置。

## 迁移后不稳定 / 需继续迁移模块
- Electricity
  - `wire/cable/pillar/catenary` 的 1.12.2 TileEntity + 自定义渲染链路（TESR/OBJ 动态渲染）尚未迁入新渲染体系。
  - 现阶段为静态方块注册基线，行为逻辑未 1:1 对齐；视觉为占位模型。
- Optics
  - 灯光/屏显/全息相关动态渲染、文字渲染、字体资源加载（`FontLoader`）与 TileEntity 逻辑尚未迁入。
  - `LightBeam`、`StationBoard`、`GuideBoard` 等当前为可注册可放置基线，不含完整交互逻辑；视觉为占位模型。
- Telecom
  - `NSPGA/SignalBox/Wireless/RSLatch/Timer/Delayer` 的设备逻辑、网络包与 GUI 流程未完成 Architectury 化。
  - 当前为 block 注册与分组接入，尚未迁移 1.12.2 的完整行为语义；视觉为占位模型。

## 当前无法直接迁移（需要重写）
- 1.12.2 `SidedProxy` 生命周期（`preInit/init/postInit`）及其事件订阅风格。
- 1.12.2 `TileEntitySpecialRenderer/FastTESR` 渲染管线（需按 1.16+ BlockEntityRenderer/现代模型流程重写）。
- 1.12.2 `SimpleNetworkWrapper`/旧包处理器直迁（需按当前网络抽象与各加载器事件总线重建）。
- 与旧外部库耦合的 NSPGA 运行时组件（需确认现代依赖、协议与许可证后再迁）。

## 下一阶段建议（按优先级）
1. 先补 Telecom 的最小可玩链路：`signal_box + input/output + timer/delayer`（不含高级 GUI），优先做服务端逻辑稳定。
2. 再补 Electricity 的连线拓扑（`wire/cable/pillar`）和基础可视化，先保证同步与保存正确。
3. 最后补 Optics 的动态渲染模块，分离“数据逻辑”和“客户端特效”，避免跨版本渲染回归。
## 增量迁移（2026-02-15）

### 新增：Telecom 运行时核心（common）
- `cn.hydcraft.hydronyasama.telecom.runtime.TelecomProcessor`
  - 迁移旧版处理器核心语义（设备注册、状态缓存、输入输出聚合、tick 更新）。
  - 已完成去 1.12.2 依赖：不再依赖 `TileEntity/World/DimensionManager`，改用回调注册（Rx sink / Tx source）。
- `cn.hydcraft.hydronyasama.telecom.signal.SignalBoxState`
  - 迁移 `SignalBox` 的基础状态推进逻辑（source connected + inverter + output）。

### 本阶段价值
- 电信迁移不再只有内容壳子，已具备可复用的逻辑核心。
- 为下一阶段“SignalBox/NSASMBox/无线收发 方块实体接线”提供稳定 common 层。

## 增量迁移（2026-02-16）

### 新增：Electricity 运行时核心（common）
- `cn.hydcraft.hydronyasama.electricity.runtime.ElectricityMath`
  - 迁移旧版 `MathAssist` 核心函数（角度三角函数、双曲函数、距离计算）。
  - 去除 1.12.2 `MathHelper` 依赖，改为纯 JDK 计算。
- `cn.hydcraft.hydronyasama.electricity.runtime.ElectricityCatenary`
  - 迁移旧版 `Wire.Catenary` 求解器（`apply/derivative/calcL/calcD`）。
  - 作为跨加载器的悬链线参数模型，供 1.16.5/1.18.2/1.20.1 客户端渲染层复用。
- `cn.hydcraft.hydronyasama.electricity.runtime.ElectricityWireGeometry`
  - 迁移旧版 `Wire` 的几何采样思路为“抽象线段输出”：
    - `buildCableSegments`（柔性线）
    - `buildHardCableSegments`（硬线）
    - `buildPillarSegments`（立柱连段）
    - `buildRailCatenarySegments`（接触网分段）
    - `buildCatenarySegments`（通用悬链线采样）
  - 输出类型为与加载器无关的 `Segment(Vec3 from,to)`，不直接依赖 TESR/BER API。
- `cn.hydcraft.hydronyasama.electricity.runtime.ElectricityLinkRuntime`
  - 迁移旧版 `sender/target` 连线语义为轻量运行时状态容器：
    - 节点注册/注销
    - sender/target 绑定与清理
    - `WIRE/CABLE/PILLAR/CATENARY` 类型标识

### 本阶段价值
- Electricity 迁移从“仅内容壳子”推进到“可复用运行时核心”。
- 后续各版本客户端只需对接本地渲染管线（BER/模型系统），无需重复实现连线数学。
- 全程未改动 `Beacon*` 代码路径，符合当前联调边界。

### 新增：Telecom 通讯全量运行时（common）
- `cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommRuntime`
  - 新增完整通信状态机，覆盖：
    - `SignalBox`
    - `SignalBoxSender`
    - `SignalBoxGetter`
    - `TriStateSignalBox`
    - `RSLatch`
    - `Timer`
    - `Delayer`
    - `WirelessRx`
    - `WirelessTx`
  - 支持旧版连线语义：
    - `sender` 链接
    - `target` 链接
    - `transceiver` 链接
  - 支持旧版行为特征：
    - 反相器
    - TriState 正/负沿触发
    - RS 锁存
    - 计时器/延时器参数（tick/自动重载）
    - 外部总线输入增减（`setExternalBusInput`）
    - 无线设备读写（`toWirelessRx` / `fromWirelessTx`）
  - 以 `snapshot()` 提供联调态观测（输入/输出/启用态/连线态）。

### 新增：Telecom 接线服务与工具链接入（2026-02-16 夜间）
- `cn.hydcraft.hydronyasama.telecom.runtime.TelecomCommService`
  - 新增通信服务门面，统一管理：
    - endpoint -> component kind 自动注册
    - `sender/target/transceiver` 连线切换
    - 编辑器参数写入（inverter/mode）
    - 平板快照查询
- 工具链接入（Fabric/Forge 1.16.5 + 1.18.2 + 1.20.1）：
  - `ConnectorItem`：第二次点击时将连线写入 `TelecomCommService`，不再只写 NBT。
  - `DevEditorItem`：将 mode/inverter 直接应用到通信运行时。
  - `NgTabletItem`：读取通信快照并写入 `telecom_tablet_scan_state` 便于联调。
- 说明：本轮仍未触碰 `Beacon*` 入口逻辑，符合“BeaconNetwork 暂缓”边界。

### 构建验证
- Fabric：
  - `:fabric-1.16.5:build -x test` 通过
  - `:fabric-1.18.2:build -x test` 通过
  - `:fabric-1.20.1:build -x test` 通过
- Forge：
  - `:forge-1.16.5:build -x test` 通过
  - `:forge-1.18.2:build -x test` 通过
  - `:forge-1.20.1:build -x test` 通过

### 增补：Telecom 服务器 Tick 驱动（不改 BeaconNetwork 内部）
- Fabric 1.16.5/1.18.2/1.20.1：在 `BeaconProviderFabric` 注册
  - `ServerTickEvents.END_SERVER_TICK -> TelecomCommService.tick()`
  - `ServerLifecycleEvents.SERVER_STOPPED -> TelecomCommService.reset()`
- Forge 1.16.5/1.18.2/1.20.1：在 `BeaconProviderForge` 注册服务端事件
  - `ServerTickEvent(END) -> TelecomCommService.tick()`
  - `ServerStopped/FMLServerStopping -> TelecomCommService.reset()`

### 二次构建验证（含 Tick 接入）
- `:fabric-1.16.5:build :fabric-1.18.2:build :fabric-1.20.1:build -x test` 通过
- `:forge-1.16.5:build :forge-1.18.2:build :forge-1.20.1:build -x test` 通过
