# Shared utilities

Embedded shared APIs for launcher context, module lifecycle/metadata, logging,
messages, colors, player/server helpers, time utilities and coroutine scheduling.

- `modules/Core.kt`, `Module.kt`: initialized plugin context and feature contract.
- `modules/ConnectionMessages.kt`: optional Server/Vanish connection-announcement hooks.
- `coroutine/`: shared IO task scope; Bukkit mutations must be scheduled separately.
- `logger/`, `message/`, `colors/`: logging and Adventure/MiniMessage helpers.
- `player/`, `server/`, `apis/`, `time/`: runtime and external-service helpers.

Not independently installable or dynamically resolved. All modules share one copy
via common's embedded runtime. Verify with `./gradlew :utils:build :core:shadowJar`.
