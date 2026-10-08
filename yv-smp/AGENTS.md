# YV SMP music integration

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and local testing.

- Gradle/artifact/manual `modules.yml` key is `yv-smp`; package is `yv.tils.yv_smp`, and `YV_SMPYVtils.MODULE.name` is `yv_smp`. Preserve these distinct identifiers when wiring lookup/version calls.
- `src/main/kotlin/yv/tils/yv_smp/YV_SMPYVtils.kt` registers music/control commands, GUI, and quit cleanup; Simple Voice Chat integration registers in late enable.
- `logic/music/` uses Lavaplayer, YouTube sources, and Opus encoding. `logic/music/svc/VoicechatBridge.kt` accesses Simple Voice Chat reflectively because dynamic library-tier code cannot see that plugin's API classes.
- Do not add `voicechat-api` dependencies or static references to its types, even `compileOnly`; use the existing bridge/proxy boundary.
- Disable fades/stops players and shuts down `VoiceChatAudioPlayer`'s executor. Preserve audio resource cleanup and player-quit behavior.
- Verification from the repository root: `./gradlew :yv-smp:build`; playback needs Paper, Simple Voice Chat, and manual `yv-smp: true` opt-in in the shared modules file.
