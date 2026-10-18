# Core launcher

The installable Paper plugin. It embeds the shared common/config/utils runtime and
Regions, resolves enabled feature libraries, selects the Minecraft-specific GUI,
and drives module load/enable/late-enable/reverse shutdown.

- `src/main/java/.../loader/`: dependency-free bootstrap, module registry/config.
- `src/main/kotlin/.../YVtils.kt`: real plugin entrypoint and lifecycle.
- `commands/`: `/yvtils info`, `/yvtils modules`, `/yvtils config` and their GUIs.
- `src/main/resources/paper-plugin.yml`: Paper metadata and optional integrations.

Java 25; supported Paper versions are 26.1.2, 26.2 and 26.3. Build with
`./gradlew :core:shadowJar`. Install `build/libs/YVtils_v<version>.jar`; enable
features in `plugins/yvtils/modules.yml`, then restart. Feature libraries must be
published before core deployment. See [release deployment](../docs/breaking-release.md).
