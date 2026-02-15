# NyaSamaCore + NyaSamaBuilding 迁移报告（阶段一基线，2026-02-15 更新）

## 目标与范围
- 目标：将 `.reference/NyaSamaCore`、`.reference/NyaSamaBuilding` 并入当前 Architectury 多版本工程，形成单模组基线。
- 阶段定义：先完成“可编译 + 可注册 + 可放置 + 关键链路可验证”，不追求 1.12.2 行为 1:1 复刻。
- 包名：统一为 `cn.hydcraft.hydronyasama.*`。

## 已完成
- `core` / `building` 基础引导：
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/CoreModule.java`
  - `common/src/main/java/cn/hydcraft/hydronyasama/building/BuildingModule.java`
  - `common/src/main/java/cn/hydcraft/hydronyasama/BeaconProviderMod.java`
- Building 元数据骨架：
  - `building/catalog/BuildingFamily.java`
  - `building/catalog/BuildingMaterialType.java`
  - `building/catalog/BuildingCatalog.java`
- Building 注册通路（已由“待迁移”升级为“已完成”）：
  - Fabric 1.16.5/1.18.2/1.20.1：`FabricContentRegistry` 已注册基础方块与衍生家族
  - Forge 1.16.5/1.18.2/1.20.1：`ForgeContentRegistry` 已完成 `DeferredRegister` 注册桥接
  - 14 类家族（`CUBE/CARPET/.../V_STRIP/WALL`）已落地并可放置
- `ItemProbe` 与命令链路（基础版）：
  - 已新增 `probe` 物品注册（Fabric/Forge + 3 版本）
  - 已新增 `/probeCmd <command...>`：将命令写入手持 Probe 的 NBT
  - 已实现“潜行右键执行已保存命令”流程
- Core 通信 `BlockEntity` 网络（基线版）：
  - 已新增 `telecom_node` 方块与 `TelecomNodeBlockEntity`（Fabric/Forge + 1.16.5/1.18.2/1.20.1）
  - 已实现频道切换、通电状态切换、邻接拓扑重建（`linkCount`）与 NBT 同步
  - 已完成跨加载器方块实体注册桥接与资源接入（`blockstates/models/lang`）
- Core 可复用基础骨架：
  - `core/train/TrainControlState.java`
  - `core/train/TrainPhysics.java`
  - `core/nsasm/NsasmEngine.java`（兼容占位）
  - `core/telecom/*` 接口族
  - `core/physics/Point3D.java`, `core/physics/Vec3.java`
- 资源迁移：`assets/hydronyasama/**` 与 `lang/*.json` 已合并（旧 `*.lang` 已移除）。

## 待补迁（高优先）
- NTP 列车控制链路
  - 现状：仅控制状态与基础物理公式。
- NSASM 真解释器与 NGT 终端
  - 现状：`NsasmEngine` 仍为兼容占位。

## 本阶段无法直接搬运
- 1.12.2 `SidedProxy + preInit/init/postInit`
- 1.12.2 `GameData.register_impl` / 旧 `ModelLoader`
- 1.12.2 `SPacketUpdateTileEntity` 旧网络路径

## 下一阶段建议
1. 优先推进 NTP 列车控制链路，完成端到端控制闭环。
2. 其次补齐 NSASM 真解释器与 NGT 终端联动。
3. 最后补迁移回归测试（重点覆盖通信节点拓扑同步与命令链路）。
