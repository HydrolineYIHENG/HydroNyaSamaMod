# Optics/Core 功能级迁移核对 - 2026-02-17

## 核对口径
- 本文按“功能是否存在”核对，不以“类名是否一致”作为唯一标准。
- 允许异名迁移、架构合并迁移（例如并入 `telecom/runtime`）。
- 状态定义：
  - `已实现`：核心行为已在现工程可定位到明确实现。
  - `部分实现`：有迁移但明显是兼容层/简化实现，或只覆盖部分平台能力。
  - `未实现`：未找到等价功能入口。

## Optics（.reference/NyaSamaOptics）核对结果

1. 光学方块/物品注册（ID 维度）
- 状态：`已实现`
- 证据：
  - `common/src/main/java/cn/hydcraft/hydronyasama/optics/content/OpticsContent.java:12`
  - `fabric-1.20.1/src/main/java/cn/hydcraft/hydronyasama/fabric/content/FabricContentRegistrar.java:127`
  - `forge-1.20.1/src/main/java/cn/hydcraft/hydronyasama/forge/ForgeContentRegistry.java:44`

2. OBJ 外形与碰撞（含多层 light 叠层）
- 状态：`已实现`
- 证据：
  - `common/src/main/resources/assets/hydronyasama/obj/optics_manifest.json:1`
  - `fabric-1.20.1/src/main/java/cn/hydcraft/hydronyasama/objrender/fabric/v120/ObjModelResourceHandler120.java:120`
  - `fabric-1.20.1/src/main/java/cn/hydcraft/hydronyasama/objrender/fabric/v120/ObjModelResourceHandler120.java:175`
  - `forge-1.20.1/src/main/java/cn/hydcraft/hydronyasama/forge/ObjCollisionBlock.java:19`

3. 薄板类方块的穿透/半透明渲染（吸附灯、导向牌、文字墙）
- 状态：`已实现`
- 说明：
  - Fabric 与 Forge 两侧均已补齐 cutout/translucent 渲染层注册。
  - 覆盖 1.16.5 / 1.18.2 / 1.20.1 三个版本。
- 证据：
  - `fabric-1.20.1/src/main/java/cn/hydcraft/hydronyasama/fabric/BeaconProviderFabricClient.java:33`
  - `fabric-1.20.1/src/main/java/cn/hydcraft/hydronyasama/fabric/BeaconProviderFabricClient.java:54`

4. 旧版 TileEntity 文本系统（Deco/Holo/TextWall/GuideBoard 的可编辑文本渲染）
- 状态：`已实现`
- 说明：
  - 已在 1.16.5/1.18.2/1.20.1 的 Fabric/Forge 落地 `text_wall/guide_board*` 方块实体与 BER 文本渲染（右键持有自定义名称物品写入文本，潜行空手清空）。
  - 1.16.5 版本已补齐 BER 注册与渲染实现。
- 证据：
  - `fabric-1.18.2/src/main/java/cn/hydcraft/hydronyasama/fabric/OpticsTextPanelBlock.java:20`
  - `forge-1.18.2/src/main/java/cn/hydcraft/hydronyasama/forge/OpticsTextPanelBlock.java:20`
  - `fabric-1.20.1/src/main/java/cn/hydcraft/hydronyasama/fabric/OpticsTextPanelBlock.java:20`
  - `fabric-1.20.1/src/main/java/cn/hydcraft/hydronyasama/fabric/OpticsTextBlockEntity.java:8`
  - `fabric-1.20.1/src/main/java/cn/hydcraft/hydronyasama/fabric/OpticsTextBlockEntityRenderer.java:10`
  - `forge-1.20.1/src/main/java/cn/hydcraft/hydronyasama/forge/OpticsTextPanelBlock.java:20`
  - `forge-1.20.1/src/main/java/cn/hydcraft/hydronyasama/forge/OpticsTextBlockEntity.java:8`
  - `forge-1.20.1/src/main/java/cn/hydcraft/hydronyasama/forge/OpticsTextBlockEntityRenderer.java:10`
  - `fabric-1.16.5/src/main/java/cn/hydcraft/hydronyasama/fabric/OpticsTextPanelBlock.java:20`
  - `fabric-1.16.5/src/main/java/cn/hydcraft/hydronyasama/fabric/OpticsTextBlockEntityRenderer.java:10`
  - `forge-1.16.5/src/main/java/cn/hydcraft/hydronyasama/forge/OpticsTextPanelBlock.java:20`
  - `forge-1.16.5/src/main/java/cn/hydcraft/hydronyasama/forge/OpticsTextBlockEntityRenderer.java:10`

5. 旧版光学专用 Renderer 类（AdBoardRenderer/GuideBoardRenderer/StationLampRenderer 等）
- 状态：`部分实现`
- 说明：已补充兼容渲染描述器类并通过 `OpticsLegacyService` 提供 profile 查询（文本墙/导向牌/站灯/平台牌等），可承接旧调用链；但尚未全量替换为旧版各 TileEntity 的原生渲染流程。

6. 字体/文本模型管线（FontLoader/TextModel/DecoTextCore）
- 状态：`部分实现`
- 说明：已补充 `optics.compat.font.FontLoader`、`optics.compat.font.TextModel` 与 `optics.compat.util.DecoTextCore`，并接入 `TileEntityDecoText/TileEntityHoloText` 兼容对象的文本模型构建；未完全复刻旧版客户端网格烘焙实现。

7. 光学网络封装与工具链（NetworkWrapper/NSOConv/ToolHandler）
- 状态：`部分实现`
- 说明：已补充 `optics.compat.network.NetworkWrapper`、`optics.compat.tool.NSOConv`、`optics.compat.event.ToolHandler`，并为 `ToolHandler` 增加对 `StationLamp/RGBLight/LEDPlate/TextWall` 的 inspect 语义与事件回调发布；仍未接入完整旧版联机协议。

## Core（.reference/NyaSamaCore）核对结果

1. Core 装饰方块（Logo/Sign）与分组
- 状态：`已实现`
- 证据：
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/content/CoreDecorContent.java:13`
  - `fabric-1.20.1/src/main/java/cn/hydcraft/hydronyasama/fabric/content/FabricContentRegistrar.java:49`
  - `forge-1.20.1/src/main/java/cn/hydcraft/hydronyasama/forge/ForgeContentRegistry.java:82`

2. 通信接口基元（IInitiative/IPassive/IReceiver/IRelay/ITransceiver）
- 状态：`已实现`
- 证据：
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/telecom/IReceiver.java:3`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/telecom/ITransceiver.java:3`

3. 数学/物理基元（Point3D/Vec3）
- 状态：`已实现`
- 证据：
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/physics/Point3D.java`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/physics/Vec3.java`

4. NSASM 能力
- 状态：`部分实现`
- 说明：已从占位态升级为可执行兼容子集（`set/let/mov/add/sub/mul/div/print/echo/halt`），可维护寄存器并输出执行结果；高级旧指令与完整虚拟机语义仍未全量复刻。
- 证据：
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/nsasm/NsasmEngine.java:8`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/nsasm/NsasmEngine.java:16`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/compat/nsasm/NSASM.java:1`

5. NGT 工具链（ItemNGT/GuiNGT/NGTPacket）
- 状态：`部分实现（架构合并迁移）`
- 说明：已迁为 `NgTabletItem + /ngt + TelecomNgScriptEngine`，并补齐 common 兼容链 `GuiNGT + ItemNGT + NGTPacket` 以承接旧类名调用；仍不等于旧版 GUI 渲染与包协议的 1:1 复刻。
- 证据：
  - `fabric-1.20.1/src/main/java/cn/hydcraft/hydronyasama/fabric/NgtCommandRegistrar.java:16`
  - `forge-1.20.1/src/main/java/cn/hydcraft/hydronyasama/forge/NgtCommandRegistrar.java:16`
  - `common/src/main/java/cn/hydcraft/hydronyasama/telecom/runtime/TelecomNgScriptEngine.java:8`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/compat/legacy/GuiNGT.java:1`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/compat/legacy/ItemNGT.java:1`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/compat/network/NGTPacket.java:1`

6. Core 旧 TileEntity 收发器链（Receiver/Transceiver/Actuator 等）
- 状态：`部分实现（并入 telecom runtime）`
- 说明：通信行为集中到 `TelecomCommRuntime/TelecomCommService`，并补齐了 legacy 适配器（`TileEntityReceiver/PassiveReceiver/SingleSender/MultiSender/Transceiver/TriStateReceiver/Actuator/NSASMCore`）以承接旧调用路径；仍非原版 TileEntity 生命周期与渲染 1:1 复刻。
- 证据：
  - `common/src/main/java/cn/hydcraft/hydronyasama/telecom/runtime/TelecomCommRuntime.java:318`
  - `common/src/main/java/cn/hydcraft/hydronyasama/telecom/runtime/TelecomCommRuntime.java:330`
  - `common/src/main/java/cn/hydcraft/hydronyasama/telecom/runtime/TelecomCommService.java:12`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/compat/legacy/LegacyTelecomTileEntity.java:1`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/compat/legacy/TileEntityReceiver.java:1`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/compat/legacy/TileEntityTransceiver.java:1`

7. 粒子包/兼容网络（ParticlePacket 等）
- 状态：`部分实现`
- 说明：存在兼容类，但是否完全覆盖旧协议链路仍需联机回归。
- 证据：
  - `common/src/main/java/cn/hydcraft/hydronyasama/telecom/compat/network/ParticlePacket.java:6`
  - `common/src/main/java/cn/hydcraft/hydronyasama/telecom/compat/network/ParticlePacketHandler.java:4`

8. 列控链路（TrainController/TrainPacket/TrainControlClientHandler/ServerHandler）
- 状态：`部分实现`
- 说明：
  - 已补齐通用兼容类：`TrainController`、`TrainPacket`、`TrainControlClientHandler`、`TrainControlServerHandler`。
  - 已补 loader 侧命令接入（`/trainctl`，六目标版本），并在服务端运行闭环中使用 `TrainPacket encode/decode` 兼容链。
  - 客户端原生网络推送仍未单独开专用 packet 通道，当前为命令驱动同步。
- 证据：
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/train/TrainPhysics.java:4`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/train/TrainControlState.java:4`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/train/TrainController.java:1`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/train/TrainPacket.java:1`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/train/TrainControlClientHandler.java:1`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/train/TrainControlServerHandler.java:1`
  - `common/src/main/java/cn/hydcraft/hydronyasama/core/train/TrainControlService.java:1`
  - `fabric-1.20.1/src/main/java/cn/hydcraft/hydronyasama/fabric/TrainCommandRegistrar.java:17`
  - `forge-1.20.1/src/main/java/cn/hydcraft/hydronyasama/forge/TrainCommandRegistrar.java:17`

## 总结
- 结论仍不是“全量完成”：
  - Optics：`基础方块与OBJ外形已迁`，`文本墙/导向牌已下沉并完成全版本 BER`，但旧版专用渲染/工具网络链仍有缺口。
  - Core：`基础装饰与接口基元已迁`，`列控兼容类+六版本命令接入已补`，但 `NSASM/NGT/列控专用网络通道` 仍有部分兼容态或未迁模块。
- 因此你这次提到的“可能功能已迁但类名不同”情况确实存在，但仅覆盖了一部分，尚不能判定为全量完迁。
