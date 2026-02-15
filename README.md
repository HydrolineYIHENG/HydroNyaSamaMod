# HydroNyaSama

> [中文](README_zh.md)

**HydroNyaSama** is a Minecraft mod project ported and maintained by the **HydroLine** development team under [HydroCraft Server](https://wiki.hydcraft.cn).

The NyaSama series of mods were once very popular in the Minecraft community but have been discontinued since 2020. Starting from December 2025, we have been porting the NyaSama series to Minecraft 1.16.5/1.18.2/1.20.1 for both Forge and Fabric loaders using ArchitecturyAPI, with plans to support more versions based on the progress of the porting.

## ⚠️ Important Note

This ported version is still in active development and may contain bugs and compatibility issues. Please make sure to regularly back up your game saves to prevent data corruption or loss.

## 🔍 About the Original Project

The HydroLine development team has limited association with the original NyaSama development team [nsdn](https://github.com/nsdn). If you encounter any issues while using this mod, please report them in this repository instead of contacting the original mod developers.

We acknowledge and respect the contributions of the original development team. We may adjust or remove certain features or textures in accordance with the requirements of the original team.

## 🎮 Download and Usage

### Stable Release
Not yet available

### Development Builds
Download from the following sources:
- HydroLine Developer Website
- GitHub Actions
- Gitee
- Cloud Native Builds

## 📋 Supported Versions

| Game Version | Forge Version | Fabric Version | Status | Notes |
|--------------|---------------|----------------|--------|-------|
| 1.16.5       | 36.0.45       | 0.14.21        | Supported | LTS |
| 1.18.2       | 39.0.10       | 0.14.21        | Supported | LTS |
| 1.20.1       | 40.0.13       | 0.15.10        | Supported | Active |

## 📂 Project Structure

```
root
├── common/           # Core business logic, protocol definitions, content declarations (Java 8, no loader dependencies)
├── fabric-1.16.5/    # Fabric adapter layer
├── fabric-1.18.2/
├── fabric-1.20.1/
├── forge-1.16.5/     # Forge adapter layer
├── forge-1.18.2/
├── forge-1.20.1/
└── docs/             # Detailed documentation
```

For more information, please refer to the [Project Structure Guide](docs/Structure.md).

## 🛠️ Build Instructions

This project uses Gradle for building.

### Build All Versions (Recommended)
```bash
./gradlew buildAllTargets
```
Artifacts can be found in the `build/libs/` directory of each subproject.

### Build Specific Version
```bash
./gradlew buildTarget_1_20_1  # Build both Forge and Fabric for 1.20.1
```

### Build Single Module
```bash
./gradlew :fabric-1.20.1:build  # Only build Fabric 1.20.1
```

## 🤝 Contribution Guide

Issues and PRs are welcome. Before adding new content, please read the [Project Structure Guide](docs/Structure.md) to understand our code organization standards.
