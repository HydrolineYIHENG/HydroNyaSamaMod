# Repository Guidelines

## Project Structure & Module Organization
- `common/src/main/java`: shared logic used by every loader build (compiled to Java 8 bytecode).
- `fabric-1.16.5`, `fabric-1.18.2`, `fabric-1.20.1`: Fabric entrypoints and resources per Minecraft version.
- `forge-1.16.5`, `forge-1.18.2`, `forge-1.20.1`: Forge entrypoints and resources per Minecraft version.
- `libs/mtr3` and `libs/create`: local mod dependency JARs used during compile.
- `docs/`: protocol and integration docs (`Beacon Actions`, `Channel API`, `Netty Gateway`).
- `tests/`: Node.js action/integration test harness and generated output snapshots.

## Build, Test, and Development Commands
- `./gradlew buildAllTargets` (`.\gradlew.bat buildAllTargets` on PowerShell): CI-equivalent build for all Fabric/Forge targets.
- `./gradlew buildTarget_1_20_1`: build both loaders for one Minecraft version.
- `./gradlew :fabric-1.20.1:build` (or any module path): build a single loader module.
- `cd tests && pnpm install`: install Node test dependencies.
- `cd tests && pnpm test:actions`: run Netty Gateway action tests and write artifacts to `tests/output`.

## Coding Style & Naming Conventions
- Language: Java (UTF-8, 4-space indentation, no tabs).
- Keep package names under `cn.hydcraft.hydronyasama`.
- Use `PascalCase` for classes, `camelCase` for methods/fields, `UPPER_SNAKE_CASE` for constants.
- Follow existing role-based suffixes, e.g. `*ActionHandler`, `*QueryGateway`, `*BeaconNetwork`.
- Keep loader-specific wiring in `fabric-*`/`forge-*`; keep reusable logic in `common`.

## Testing Guidelines
- Primary gate: successful `buildAllTargets`.
- Integration coverage lives in `tests/test-actions.js` (Gateway actions such as MTR queries).
- When adding/changing actions, extend the Node test flow and verify output files in `tests/output` are produced and parseable.
- Run tests against a live provider configured through `config/hydroline/HydroNyaSama.json` (host/port/token).

## Commit & Pull Request Guidelines
- Follow Conventional Commit style seen in history: `feat(scope): ...`, `fix: ...`, `chore(scope): ...`, `docs(scope): ...`.
- Keep commits focused to one logical change and include affected module/version in scope when possible.
- PRs should include: purpose, impacted modules (for example `forge-1.20.1`), linked issue, and validation steps run.
- For protocol/action changes, attach sample request/response output and update relevant docs in `docs/`.

## Security & Configuration Tips
- Do not commit private tokens/endpoints from local config or `tests/.env`.
- If dependency JAR names/locations change in `libs/`, update mappings in `build.gradle.kts` in the same PR.
