# Private messaging

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and shared-runtime rules.

- `src/main/kotlin/yv/tils/message/MessageYVtils.kt` replaces vanilla `w`, `whisper`, `msg`, and `tell`, then registers `MSGCommand` and `ReplyCommand`.
- `logic/MessageHandler.kt` owns in-memory UUID reply sessions. Player sends update both participants; console sends do not create a player session. Quit removes sessions and module disable clears them.
- Message rendering uses config-v2 translations separately for sender and recipient; preserve recipient locale handling and existing placeholder keys when changing formatting.
- Verification from the repository root: `./gradlew :message:build`; exercise reply, quit cleanup, console, and aliases on Paper.
