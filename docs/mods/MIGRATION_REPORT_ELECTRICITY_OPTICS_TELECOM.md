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
