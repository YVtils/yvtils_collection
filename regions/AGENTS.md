# WorldGuard-backed survival claims

Follow [root AGENTS.md](../AGENTS.md) for toolchain and the static-bundling/classloader exception. Read [README.md](README.md) for claim, role, pricing, subzone, and GUI semantics before changing them.

- `src/main/kotlin/yv/tils/regions/RegionsYVtils.kt` checks enabled WorldGuard/API access before activation. Regions is bundled in core, not dynamically published; retain WorldGuard `compileOnly` and core's joined plugin dependency.
- WorldGuard stores geometry, flags, and domains; `data/ClaimMetadata.kt` owns `/regions/claims.json` metadata keyed by UUID. Keep changes through claim services so protection and metadata stay aligned.
- A claim's primary `yv2_<uuid>` cuboid and role-policy cuboids must be maintained together. Policy domains are implementation details, not extra ownership grants. Preserve save failure rollback and unloaded-world metadata accounting.
- `commands/RegionAlias.kt` routes `/rg` using effective WorldGuard command permissions; protection bypass alone must not select WorldGuard. `/regions` addresses YVtils directly.
- `logic/ClaimOccupancy.kt` provides main-thread live-location queries. Selection, preview, occupancy, and alias cleanup run on disable.
- Verification: `./gradlew :regions:test :regions:build :core:shadowJar`. Tests use WorldGuard's real flag calculator; GUI/protection interactions still need Paper plus WorldGuard/WorldEdit.
