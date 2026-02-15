# Repository Guidelines

## Project Structure & Module Organization
- `common/src/main/java`: shared Java logic compiled for all loaders/versions.
- `fabric-1.16.5`, `fabric-1.18.2`, `fabric-1.20.1`: Fabric entrypoints, version wiring, and resources.
- `forge-1.16.5`, `forge-1.18.2`, `forge-1.20.1`: Forge entrypoints, version wiring, and resources.
- `libs/mtr3` and `libs/create`: local dependency JARs used at compile time.
- `docs/`: protocol and integration docs (`Beacon Actions`, `Channel API`, `Netty Gateway`).
- `tests/`: Node.js integration harness and generated snapshots in `tests/output`.

## Build, Test, and Development Commands
- `./gradlew buildAllTargets` (PowerShell: `.\gradlew.bat buildAllTargets`): CI-equivalent build for all Fabric/Forge targets.
- `./gradlew buildTarget_1_20_1`: build both loaders for Minecraft 1.20.1.
- `./gradlew :fabric-1.20.1:build`: build a single module.
- `cd tests && pnpm install`: install Node test dependencies.
- `cd tests && pnpm test:actions`: run Netty Gateway action tests and write outputs.

## Coding Style & Naming Conventions
- Language: Java, UTF-8, 4-space indentation, no tabs.
- Packages stay under `cn.hydcraft.hydronyasama`.
- Naming: `PascalCase` classes, `camelCase` methods/fields, `UPPER_SNAKE_CASE` constants.
- Follow role-based suffixes (for example `*ActionHandler`, `*QueryGateway`, `*BeaconNetwork`).
- Keep reusable logic in `common`; keep loader-specific wiring in `fabric-*`/`forge-*`.

## Testing Guidelines
- Primary gate: successful `buildAllTargets`.
- Extend `tests/test-actions.js` when adding/changing gateway actions.
- Verify files in `tests/output` are generated and parseable.
- Integration tests require a live provider config in `config/hydroline/HydroNyaSama.json` (host/port/token).

## Commit & Pull Request Guidelines
- Use Conventional Commits: `feat(scope): ...`, `fix: ...`, `chore(scope): ...`, `docs(scope): ...`.
- Keep commits focused to one logical change; include module/version scope when possible.
- PRs should include purpose, impacted modules (for example `forge-1.20.1`), linked issue, and validation steps.
- For protocol/action changes, include sample request/response output and update related docs in `docs/`.

## Security & Configuration Tips
- Never commit private tokens/endpoints from local config or `tests/.env`.
- If JAR names/locations change under `libs/`, update `build.gradle.kts` mappings in the same PR.
