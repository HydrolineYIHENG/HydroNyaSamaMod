# HydroNyaSama

> [English](README.md)

**HydroNyaSama** 是由 [氢气工艺服务器 HydCraft](https://wiki.hydcraft.cn) 下属的 **HydroLine开发团队** 移植与维护的 Minecraft 模组项目。

NyaSama 喵玉系列 (含 NyaSamaTelecom 通信、NyaSamaBuilding 土木、NyaSamaElectricity 电力、NyaSamaOptics 光学、NyaSamaRailway 铁道等组件) 模组曾在 Minecraft 社区广受欢迎，但自 2020 年起停止更新。2025 年 12 月起，我们借助 ArchitecturyAPI 开始将 NyaSama 系列模组移植到 Minecraft 1.16.5/1.18.2/1.20.1 版本的 Forge 和 Fabric 加载器上，后续将根据移植情况支持更多版本。

## ⚠️ 重要提示

本移植版本仍处于开发阶段，可能存在 BUG 和兼容性问题。请务必定期备份您的游戏存档，以防数据损坏或丢失。

## 🔍 关于原项目

HydroLine 开发团队与原 NyaSama 喵玉系列模组的 [喵玉殿](https://github.com/nsdn) 开发团队几乎没有关联。使用过程中如遇任何问题，请在本项目仓库反馈，而非联系原模组开发者。

HydroLine 团队感谢原开发团队的贡献，并尊重相关版权。我们可能会根据原团队的要求，对部分玩法或贴图进行调整或移除。


## 🎮 获取与使用

### 正式版本
暂未发布

### 开发版本
你可通过以下途径下载到开发版本的模组 Jar：
- HydroLine 开发者官网
- GitHub Actions
- Gitee
- 腾讯云·云原生构建

## 📋 支持版本

| 游戏版本 | Forge 版本号 | Fabric 版本号 | 支持状态 | 备注 |
|---------|-------------|--------------|----------|------|
| 1.16.5  | 36.0.45     | 0.14.21      | 已支持   | LTS  |
| 1.18.2  | 39.0.10     | 0.14.21      | 已支持   | LTS  |
| 1.20.1  | 40.0.13     | 0.15.10      | 已支持   | Active |

## 📂 项目结构

```
root
├── common/           # 核心业务逻辑、协议定义、内容声明（Java 8，无加载器依赖）
├── fabric-1.16.5/    # Fabric 适配层
├── fabric-1.18.2/
├── fabric-1.20.1/
├── forge-1.16.5/     # Forge 适配层
├── forge-1.18.2/
├── forge-1.20.1/
├── libs/             # 外部依赖（MTR API, Create API）
└── docs/             # 详细文档
```

如需更多信息，请参考 [项目结构指南](docs/项目结构解读指南.md)。

## 🛠️ 构建指南

本项目使用 Gradle 构建系统。

### 构建所有版本（推荐）
```bash
./gradlew buildAllTargets
```
构建产物位于各子工程的 `build/libs/` 目录下。

### 构建特定版本
```bash
./gradlew buildTarget_1_20_1  # 构建 1.20.1 版本的 Forge 和 Fabric 两个变体
```

### 构建单个模块
```bash
./gradlew :fabric-1.20.1:build  # 仅构建 Fabric 1.20.1 版本
```

## 🤝 贡献指南

欢迎提交 Issue 或 PR 参与项目改进。在添加新内容前，请先阅读 [项目结构解读指南](docs/项目结构解读指南.md) 以了解我们的代码组织规范。
