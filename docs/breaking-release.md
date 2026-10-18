# Breaking-release deployment

This release is a fresh installation of the collection. Legacy plugin folders,
permission grants, claim stores, recipes and addon APIs are not supported upgrade
inputs. No automatic migration from standalone plugins is provided. Keep the old
deployment separately if rollback is required.

Legacy general-purpose event logging and legacy command aliases/permissions are
retired. Inventory editing uses the new permission-gated online/offline editor;
see the current command/permission descriptions in the README.

## Installation

1. Use Java 25 and Paper 26.1.2, 26.2 or 26.3.
2. Install only `core/build/libs/YVtils_v<core-version>.jar` as the collection
   plugin. Feature JARs are Maven libraries, not Bukkit plugins.
3. Start once, then edit `plugins/yvtils/modules.yml` and restart. Normal modules
   default to disabled. The GUI matching the server version is always resolved.
4. Install WorldGuard and its WorldEdit dependency before enabling Regions.
5. Configure the Discord module's token/guild/channels before using its features.
   Discord connects asynchronously; local account inspection remains available
   while the external connection is unavailable.
6. `yv-smp` is manual opt-in: add `yv-smp: true` to the shared module file.
   Voice playback requires Simple Voice Chat; ambient sounds do not.

## Release artifacts

`gradle/module-versions.properties` supplies every artifact version and the core
registry. Publish all feature and GUI coordinates before distributing core. Never
reuse published coordinates for changed code. All GUI variants compile the shared
`gui-26.1` source tree and must receive fresh versions together when it changes.

The publication task verifies JAR checksums before skipping existing coordinates;
an existing artifact with different content fails publication. Archives use stable
ordering and omit timestamps. A fresh coordinate is required to repair an old
incompatible artifact. Local publishing remains a developer operation and does not
publish to the production repository.

`./gradlew build --no-daemon` runs the complete build and test suite on Java 25.
PR/push CI runs the same command. Compiler heap and worker limits are committed in
`gradle.properties` so the default command is reproducible.

## Failure behavior

- A module lifecycle exception aborts collection startup. Cleanup is attempted for
  every initialized module in reverse order; one cleanup failure does not skip others.
- Existing malformed/unreadable typed state fails loading. It is not replaced with
  defaults. Repair the reported file and restart. Missing files receive defaults.
- Configuration writes use same-directory temporary files and atomic replacement.
  Filesystems must support atomic rename.
- MultiMine uses Paper's player break operation for every chained block, including
  event cancellation, drops, experience and durability. A protected block ends that
  branch of mining.
- `/yvsmp stop`, participant disconnect and plugin shutdown cancel owned cinematic
  callbacks and restore borrowed player state. Destructive completed gameplay phases
  (such as item clearing) are not rolled back.

## Staging acceptance

Run these checks against each exact supported Paper version and the corresponding
published GUI artifact, using fresh server library caches:

- Core-only boot; enable/disable modules across restarts; inspect `/yvtils modules`
  and config editors. Verify module versions and GUI classes come from intended JARs.
- Place eligible ore/logs across a WorldGuard boundary. Mine from the permitted
  side: denied blocks must remain intact with no drops or durability charge. Test
  concurrent players, broken tools, swapped tools, disconnects and break limits.
- Make a typed save malformed or unreadable: startup must fail and original bytes
  must remain. Restore it and verify statuses, punishments, toggles and settings.
- Switch player locale between German/English and configure console/default language,
  debug settings, timezone and server address. Custom translations survive restarts.
- Deny each new command permission; test Vanish layers, joining/quitting, mob targets,
  pickup/drop, attacks and silent read-only containers. Disable restores visibility.
- Create private/public/unlisted waypoints with multiple owners, list them, navigate,
  change worlds, arrive, stop, reconnect and restart. Verify block-display privacy
  with another client and cleanup without real-block changes or chunk loading.
- Inspect online/offline inventories and ender chests; test permitted slot exchanges,
  denied edit permission, stale clicks, unsupported bulk operations, external file
  changes, failed writes and target login while an offline session is open.
- Start Discord with a bad token and unavailable gateway; Minecraft must finish
  startup. Stop while connecting, and verify no orphan JDA connection remains.
  Complete the detailed Discord interaction checklist in `discord-registration.md`.
- Abort the SMP sequence during warmup and every phase, disconnect a participant,
  and stop the server. Verify camera removal, game modes, flight, effects, visibility,
  scoreboard and location restoration; stale callbacks must not restart effects.
- Complete Dragon encounter/recovery and Fusion inventory/recipe acceptance checks
  in their module guides.

Automated tests do not replace these Paper/client and external-service checks.
