# Fabric OBJ 接入方案

## 目标
在 Fabric `1.16.5`、`1.18.2`、`1.20.1` 三个版本上支持光学模块的 OBJ 模型渲染，不依赖 Forge 专有的 `forge:obj` 加载器。

## 当前状态
- Forge 三版本已对关键光学方块接入 `loader: forge:obj`。
- common 资源中已包含光学 OBJ 与贴图依赖。
- Fabric 三版本已具备客户端入口，可用于注册渲染链。

## Fabric 推荐技术路线
1. 模组内置 OBJ 中间层：
   - 解析 `.obj` 并缓存网格数据。
   - 从方块图集解析贴图并绑定 UV。
   - 通过 Fabric 客户端渲染接口绘制。
2. 版本适配层最小化：
   - 共享同一套解析与缓存逻辑（common）。
   - 仅在各 Fabric 版本编写薄注册/渲染适配层。
3. 使用统一模型映射清单：
   - `assets/hydronyasama/obj/optics_manifest.json`

## 资源包覆盖支持
该方案支持资源包覆盖 OBJ/贴图：
- 渲染层按 `ResourceLocation` 读取模型与贴图资源。
- 当资源包替换同路径资源后，渲染结果会随资源包变化。

## 前置模组评估
- `Special Model Loader` 更偏新版本，不覆盖完整 `1.16.5~1.20.1` 矩阵。
- 旧版 Fabric OBJ Loader 存在，但版本覆盖分裂，难以统一维护。
- 对本仓库目标版本组合而言，“模组内置中间层”可控性和稳定性最高。
