# Private messaging

Private player messages and replies through `/msg` and `/r`/reply commands.

Use `/msg <player> <message>` for a private message or `/msg @a <message>` to
message all online players. Standard player selectors are supported. The aliases `message`, `tell`, `whisper`, `dm`,
and `w` support both forms.

| Permission | Default | Allows |
| --- | --- | --- |
| `yvtils.message.command.msg` | Everyone | `/msg` and its aliases |
| `yvtils.message.command.msg.all` | Operators | `@a` and selections with multiple recipients (also requires the msg permission) |
| `yvtils.message.command.reply` | Everyone | `/reply` and `/r` |
| `yvtils.message.receive` | Everyone | Receiving messages, including replies and all-player messages |
| `yvtils.message.*` | Operators | All message permissions |

All-player messages skip recipients without the receive permission. Direct
messages and replies to these recipients report that delivery is unavailable.

- `MessageYVtils.kt`: dynamic lifecycle and command registration.
- `commands/`: message and reply trees.
- `logic/MessageHandler.kt`: delivery and last-contact tracking.
- `language/`: registered message translations.

Enable `message` in the shared module file. Verify with `./gradlew :message:build`;
test replies, offline recipients and permission behavior with Paper players.
