# OBJRender 接口说明

## 目标
统一 Fabric 多版本（1.16.5 / 1.18.2 / 1.20.1）的 OBJ 模型接入入口，避免把模型加载逻辑散落在业务模块中，方便后续继续接入 Forge 或资源包扩展。

## 包结构
- 公共接口：`cn.hydcraft.hydronyasama.objrender.api`
- Fabric 1.16.5 实现：`cn.hydcraft.hydronyasama.objrender.fabric.v116`
- Fabric 1.18.2 实现：`cn.hydcraft.hydronyasama.objrender.fabric.v118`
- Fabric 1.20.1 实现：`cn.hydcraft.hydronyasama.objrender.fabric.v120`

## 统一入口
- 接口：`ObjRenderClientBootstrap`
- 方法：`initialize()`
- 用法：由各版本 `BeaconProviderFabricClient` 在客户端初始化阶段调用。

## 1.20.1 当前实现
- 已接入 `forge:obj` JSON 兼容加载（读取模型 JSON 中 `loader/model/textures`）。
- 已接入 OBJ 解析与烘焙网格（基于 Fabric Renderer API + `de.javagl.obj`）。
- 已从光学 BER 手动三角渲染切换为模型加载阶段烘焙，减少闪烁、透明面和缺面问题。

## 1.16.5 / 1.18.2 状态
- 已建立版本实现入口类（占位），统一调用链已打通。
- 后续将按 1.20.1 同一接口补齐对应 Model Provider/Loader 细节。

## 后续扩展建议
- 增加 `loader` 扩展点（如 `hydronyasama:obj`）以兼容非 Forge 风格 JSON。
- 抽离材质解析策略（MTL 优先，`textures.particle` 回退）到公共策略层。
- 预留资源包覆盖支持：允许通过资源包替换 OBJ/贴图并由同一加载链路自动生效。
