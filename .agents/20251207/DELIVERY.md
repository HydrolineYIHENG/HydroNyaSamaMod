# 交付说明

## 1. 模块与目录结构
- `common/`：所有 Loader 复用的业务逻辑与 Logger，纯 Java 8 目标，便于共享字节码，Jar 会被各 Loader 模块在打包阶段直接合入。
- `fabric-*/`：按 Minecraft 版本划分的独立 Fabric 入口，均依赖对应的 Fabric Loader / Fabric API 版本，使用 `fabric.mod.json` 固定单一 `minecraft` 版本。
- `forge-*/`：与 Fabric 同样的按版本拆分目录，Forge 入口直接调用 `BeaconProviderMod`，保留 `mods.toml` 与 `pack.mcmeta` 以匹配目标版本。
- `gradle/` + `gradlew*`：沿用官方 Wrapper，默认 8.7；可以直接调用系统 Gradle，Wrapper 作为兜底。

根目录已经剔除了 `.gradle-user-home/`、`gradle-8.7/`、`template/` 等历史残留，仓库即开即用。

## 2. 构建矩阵
| 目录 | Loader | Minecraft | Java | Fabric Loader | Fabric API | Forge |
| --- | --- | --- | --- | --- | --- | --- |
| `fabric-1.16.5` | Fabric | 1.16.5 | 8 | 0.14.23 | 0.42.0+1.16 | - |
| `fabric-1.18.2` | Fabric | 1.18.2 | 17 | 0.14.23 | 0.76.1+1.18.2 | - |
| `fabric-1.20.1` | Fabric | 1.20.1 | 17 | 0.15.10 | 0.92.1+1.20.1 | - |
| `forge-1.16.5` | Forge | 1.16.5 | 8 | - | - | 1.16.5-36.2.39 |
| `forge-1.18.2` | Forge | 1.18.2 | 17 | - | - | 1.18.2-40.2.21 |
| `forge-1.20.1` | Forge | 1.20.1 | 17 | - | - | 1.20.1-47.1.3 |

`common` 始终使用 Java 8 编译，可被所有 Loader 无缝复用。

## 3. 构建命令
- 单个模块：`./gradlew :fabric-1.20.1:build` 或 `./gradlew :forge-1.18.2:build`。
- 某个版本的整套 Loader：`./gradlew buildTarget_1_18_2`（内部会串行执行该版本的 Fabric/Forge 子任务）。
- 全部版本：`./gradlew buildAllTargets`。

触发 Loader 模块的 `Jar` 任务时会自动将 `:common` 的 classes/resources 合入，成品位于各模块 `build/libs/`。

## 4. 设计要点
1. **按版本拆分目录**：每个版本对应独立的 Gradle 子工程，方便增删版本、不互相污染依赖。
2. **共享逻辑最小化**：`common` 不再依赖 Architectury，仅保留真正跨 Loader 的逻辑，降低 remap/transform 的复杂度。
3. **集中式构建脚本**：`build.gradle.kts` 统一声明矩阵、自动为每个子项目注入 Loom/Fabric/Forge 依赖，避免重复脚本。
4. **干净的输出**：`.gitignore` 针对离线缓存、临时脚本和各种 `build/` 目录做了精确过滤，仓库保持可读性。

如需新增目标版本，只需在 `build.gradle.kts` 的 `supportedTargets` 中登记版本参数，并复制对应 Fabric/Forge 目录的资源文件即可。
