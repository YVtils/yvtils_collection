# Private messaging

Private player messages and replies through `/msg` and `/r`/reply commands.

- `MessageYVtils.kt`: dynamic lifecycle and command registration.
- `commands/`: message and reply trees.
- `logic/MessageHandler.kt`: delivery and last-contact tracking.
- `language/`: registered message translations.

Enable `message` in the shared module file. Verify with `./gradlew :message:build`;
test replies, offline recipients and permission behavior with Paper players.
