# YV SMP ambient sound sources

Named sound sources are managed with `/yvsmp ambient`. Creation and movement use
the player's current position; other operations also work from the console.
Permission: `yvtils.yv_smp.command.ambient` (operators by default).

```text
/yvsmp ambient create forest minecraft:ambient.cave 24 true 30s
/yvsmp ambient set forest particle minecraft:firefly
/yvsmp ambient set forest sound minecraft:block.amethyst_block.chime
/yvsmp ambient set forest radius 32
/yvsmp ambient set forest looping false
/yvsmp ambient set forest interval 2m
/yvsmp ambient play forest
/yvsmp ambient info forest
/yvsmp ambient list
/yvsmp ambient move forest
/yvsmp ambient remove forest
/yvsmp ambient set forest particle none
```

- Names are case-sensitive and contain 1–64 letters, numbers, `_` or `-`.
- Sounds accept Minecraft namespaced IDs, including custom resource-pack IDs.
  Vanilla sounds are suggested by tab completion. Custom sounds require the
  corresponding client resource pack.
- Radius is a three-dimensional sphere, from 0.1 to 512 blocks in the same world.
  Playback is positional, with normal client attenuation. Sound packets are sent
  only to players in range, and tracked playback is stopped when players leave.
  Minecraft stops sounds by sound ID/category, so stopping one source can also
  stop another source using the same sound for that listener until its next play.
- Intervals accept ticks (`40t`), seconds (`10s`), minutes (`5m`), or hours (`1h`).
  Full unit names are supported, and bare numbers mean ticks. Decimal values are
  accepted if they resolve to a positive whole number of ticks (e.g. `0.5s`).
  Timing follows server ticks, including server lag. The interval is between
  playback starts; choose one at least as long as the sound to avoid overlap.
- Creating a source plays it once immediately. Looping sources repeat after
  their interval and resume automatically after restart. Non-looping sources
  remain saved and can be triggered again with `play`; they do not replay on
  restart or player entry. A player entering a looping source hears its next play.
- Editing a source stops its tracked playback and resets its loop countdown.
- Optional particles are sampled throughout the sphere once per second (eight
  points), visible to players inside it. Every Minecraft particle type can be
  selected; types requiring extra data use defaults such as white dust, stone
  blocks, or amethyst items. `none` disables particles.
- Sources save immediately to `plugins/yvtils/yv_smp/ambient-sources.yml` using
  config. Sources in unloaded worlds stay dormant until those worlds load;
  they do not force-load worlds or chunks. Particle effects continue independently
  of whether sound playback is looping.

This system uses Minecraft's sound engine and works without Simple Voice Chat.

## Verification

Run `./gradlew :yv-smp:build` with JDK 25. On a Paper server with `yv-smp` enabled,
create a short looping source and check playback inside/outside its radius, world
changes, edits, particle types (including `dust`, `block` and `vibration`), removal,
and persistence across restart. Verify an unprivileged player cannot manage
sources. Deploy through the core launcher and dynamic-module repository as
described in the root `AGENTS.md`.
