# Sit

`/sit` toggles sitting at the player's current position, using an invisible
gravity-free seat entity. Dismount and quit listeners remove the seat.

- `SitYVtils.kt`: lifecycle and command/listener registration.
- `commands/SitCommand.kt`: `/sit` and `/chair`.
- `logic/SitManager.kt`: seat creation, offsets and player tracking.
- `listeners/`: dismount/disconnect cleanup.

Enable `sit`; no separate module config is currently exposed.
Verify with `./gradlew :sit:build` and entity/dismount checks on Paper.
