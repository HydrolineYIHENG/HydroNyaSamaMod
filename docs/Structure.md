# HydroNyaSama 源码结构与开发要点

## 一、开发使用方式

- `./gradlew buildAllTargets` 会自动构建 Fabric/Forge + `common` 版本，入口在根工程的 `build.gradle.kts` `loaderProjects` 列表。
- Architectury API 由 `downloadArchitecturyJar` 任务下载到 `checkouts/architectury-<version>.jar`，构建时自动依赖；Create（1.20.1）的 slim 版仍由 `https://maven.createmod.net` 拉取。
- `common` 模块编译时引用 `MTR-forge-1.20.1-3.2.2-hotfix-2-slim.jar`，保证核心逻辑可以直接使用 MTR API；构建时也会通过 `architecturyApi` 配置获得 Architectury API。

## 二、目录与依赖说明

```
.
├─ checkouts/
│  └─ architectury-<version>.jar
├─ libs/
│  ├─ create/
│  │  └─ create-1.18.2-0.5.1.f.jar  # Create 1.18.2 用于 Forge-1.18.2 构建
│  └─ mtr3/
│     ├─ MTR-fabric-1.16.5-3.2.2-hotfix-1-slim.jar
│     ├─ MTR-fabric-1.18.2-3.2.2-hotfix-1-slim.jar
│     ├─ MTR-fabric-1.20.1-3.2.2-hotfix-1-slim.jar
│     ├─ MTR-forge-1.16.5-3.2.2-hotfix-1-slim.jar
│     ├─ MTR-forge-1.18.2-3.2.2-hotfix-1-slim.jar
│     └─ MTR-forge-1.20.1-3.2.2-hotfix-2-slim.jar
├─ common/                    # 共用逻辑模块
├─ fabric-.../forge-.../      # 各版本 loader 项目
├─ docs/Structure.md          # 本说明文档
├─ build.gradle.kts           # 构建入口，处理 loader 注册、Jar 映射与依赖注入
├─ gradle.properties         # 版本与手动依赖设置
```

- `build.gradle.kts` 中通过 `mtrJarMap` 将每条 loader + Minecraft 版本自动关联到 `libs/mtr3/` 内的 jar（需手动下载并保持名称一致）。
- Architectury API 不再直接依赖 Maven 组，而是通过 `downloadArchitecturyJar` 下载到 `checkouts/architectury-<version>.jar`；Create 1.20.1 的 slim 包仍由 `https://maven.createmod.net` 提供，1.18.2 版本依赖 `libs/create/create-1.18.2-0.5.1.f.jar`。
- loader 项目通过 shadowJar 将 Architectury API Relocate 到 `cn.hydcraft.hydronyasama.shaded.architectury`，避免与 MTR 或其他 mod 冲突；JAR 名称统一为 `archivesBaseName-loader-minecraftVersion`。
- `common` 模块只依赖于 Architectury + 固定的 MTR jar，因此可以在任何 loader 中共享，只需确保所有 loader 引入同一份 `common` 代码。

## 三、MTR 与 Create API 使用建议

- **MTR**：本 mod 通过 `compileOnly` 方式绑定 `libs/mtr3/` 中的 loader jar，运行时需保证目标环境实际已经装了对应的 MTR（即 Forge/Fabric + 对应版本的 mod）。可以直接调用 `RouteMap`、`Route`、`Station`、`Depot`、`Node` 等类，封装为 DTO 后经 Bukkit Channel 传给外部服务。
- **Create**：1.18.2/1.20.1 的 Forge 子项目通过 `modImplementation("com.simibubi.create:create-<mcVersion>:<createVersion>:slim")` 注入 Create API，用于访问机械动力/水力设备等；Create jar 由 `https://maven.createmod.net` 下载，Gradle 会自动缓存。
- 考虑到 `common` 需要兼容多个 loader，建议在 `common` 中只定义接口/事件（如 `BeaconRouteProvider`），把具体依赖 Create 的逻辑放在 Forge 子项目，让 Bukkit 插件只对 `common` 暴露的 API 依赖。
- 需要轮询/监听 MTR 内部数据时可以订阅相关事件（如 `RouteNodeEvent`、`RouteUpdateEvent`）或定时读取 `RouteMap`，将线路、车站、车厂、节点属性整理后通过 Minecraft channel 发送。

## 四、扩展与维护提示

1. 新增 Minecraft 版本时：
   - 下载对应的 `MTR-<loader>-<mcVersion>-<mtrVersion>-slim.jar`（仅保留 `mtr/data/**` 的 API classes）放入 `libs/mtr3/`，文件名与 `mtrJarNameMap` 保持一致；
   - 在 `build.gradle.kts` 中添加对应的 target 配置与 `createVersionMap` 条目（若该版本需要 Create）；
   - 确保 loader 工程的 Java Toolchain、Loom 版本与目标 Minecraft 匹配。
2. Architectury 升级：只需修改 `architecturyVersion`（`gradle.properties` 中设置），`downloadArchitecturyJar` 会从 `https://maven.architectury.dev/dev/architectury/architectury` 下载安装包到 `checkouts/`。
3. Create 升级：调整 `createVersion_1_18_2` / `createVersion_1_20_1` 后，Gradle 会从 `https://maven.createmod.net` 拉取对应的 `:slim` 版本，无需手动放入 `libs`。
4. 任何需要额外的 loader-specific 依赖（例如 Bukkit channel、Shim 模块）都应为其创建独立初始化类，由 loader 项目注入并调用，避免在 `common` 中引入不会在另一个 loader 上可用的代码。
5. MTR API 更新：若 `libs/mtr3/` 中的原始 MTR JAR 变更，可以运行 `tools/extract_mtr_api.sh` 重新抽取 `mtr/data`，脚本会生成带 `-slim.jar` 后缀的 API 文件，同步到 `mtrJarNameMap` 即可继续编译。
