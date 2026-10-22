# YV SMP ambient sound sources

## Module overview and structure

Optional `yv-smp` provides staged SMP-start cinematics, border/setup controls,
Simple Voice Chat music and persistent ambient sound sources. Enable it manually
with `yv-smp: true`; its runtime name/data folder is `yv_smp`.

- `YV_SMPYVtils.kt`: module lifecycle and command/listener wiring.
- `commands/`, `gui/`: start/setup/music/ambient administration.
- `logic/start/`, `utils/`: cinematic phases, owned scheduler/camera cleanup and particles.
- `logic/music/`: audio loading/playback and reflective Simple Voice Chat bridge.
- `logic/ambient/`: persistent Minecraft sound/particle and positional SVC sources.
- `configs/`, `language/`, `permissions/`: settings, feedback and access policy.

Build with `./gradlew :yv-smp:build`. Music requires Simple Voice Chat; ambient
effects do not. The commands below describe ambient source administration.

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
  playback starts for each listener; repeating replaces that listener's previous
  playback. Set it to the track length for full-track loops; shorter intervals
  restart the track early, and longer intervals leave silence between repeats.
- Creating a source plays it once immediately. Looping sources repeat after
  their interval and resume automatically after restart. Non-looping sources
  remain saved and can be triggered again with `play`; they do not replay on
  restart or player entry. Entering a looping source starts playback immediately,
  then repeats on that listener's own interval, even if the source was previously
  empty. Leaving stops playback; re-entering starts it again. Vanilla sound packets
  cannot seek, so new listeners hear the beginning rather than joining mid-track.
- Editing a source stops its tracked playback. Looping sources restart for nearby
  listeners on the next tick.
- Optional particles are sampled throughout the sphere once per second (eight
  points), visible to players inside it. Every Minecraft particle type can be
  selected; types requiring extra data use defaults such as white dust, stone
  blocks, or amethyst items. `none` disables particles.
- Sources save immediately to `plugins/yvtils/yv_smp/ambient-sources.yml` using
  config. Sources in unloaded worlds stay dormant until those worlds load;
  they do not force-load worlds or chunks. Particle effects continue independently
  of whether sound playback is looping.

The `create`/`set … sound` sources use Minecraft's sound engine and work without
Simple Voice Chat.

## Shared positional music with Simple Voice Chat

For jukebox-like music where everyone hears the **same point in the track**, use
an SVC audio source:

```text
/yvsmp ambient create-audio plaza 12 true plugins/yvtils/music/plaza.ogg
/yvsmp ambient create-audio river 8 true https://example.com/river.mp3
/yvsmp ambient set forest audio plugins/yvtils/music/forest.ogg
/yvsmp ambient set plaza radius 6
/yvsmp ambient set plaza particle minecraft:firefly
/yvsmp ambient play plaza
```

- Requires Simple Voice Chat on the server and the client mod connected for each
  listener. Volume uses the module's master volume and SVC's **YVtils** category.
- Each source owns one locational stream at its saved position. Volume attenuates
  with distance up to its configured radius, in the same world. Entering range,
  joining, reconnecting voice chat or teleporting into range joins the ongoing
  stream; it does not restart the track for anyone.
- The stream advances even with no nearby players. `looping true` repeats at the
  actual end of the track, independently of the Minecraft-sound `interval` field.
  Live streams without an end naturally keep playing.
- Input must be a real server-side audio file or an HTTP(S) URL supported by
  LavaPlayer (for example OGG, WAV or MP3). Paths are relative to the server working
  directory; spaces are allowed in the final audio argument. Minecraft sound IDs
  and resource-pack IDs cannot be converted into SVC audio automatically: supply
  the corresponding audio file on the server.
- `set <name> audio <file or URL>` converts an existing source while preserving its
  position/radius/particles. `set <name> sound <Minecraft ID>` switches it back.
- Creation and `play` start/restart the shared track. Looping audio sources resume
  after server restart once SVC is ready and their world is loaded. Non-looping
  audio plays once when explicitly triggered. Edits stop existing playback and
  restart looping sources; removal, world unload and module disable stop streams
  and release their resources. Worlds and chunks are never force-loaded.
- Loading/decoding failures are logged and stop the stream without continuously
  retrying. Correct the file/URL and use `play` or edit the source to retry.

## Verification

Run `./gradlew :yv-smp:build` with JDK 25. On a Paper server with `yv-smp` enabled,
create a short looping source and check playback inside/outside its radius, world
changes, edits, particle types (including `dust`, `block` and `vibration`), removal,
and persistence across restart. Verify an unprivileged player cannot manage
sources. Deploy through the core launcher and dynamic-module repository as
described in the root `AGENTS.md`.

For SVC, use two modded clients and a short audio file. Start with nobody in range,
wait through several track ends, then enter with both clients at different times:
both should hear the current shared position. Check attenuation at the configured
radius, reconnect/join/world changes, looping without listeners, a non-looping
track, URL/file failures, edits/removal and restart/disable cleanup.
