# Common runtime

Collection-wide settings, translations, locale listeners, permission registration,
action announcements and update checking. Embedded by core, not independently installed.

- `CommonYVtils.kt`: lifecycle and shared listener registration.
- `config/`: typed root settings and flattened compatibility accessors.
- `language/`, `listeners/`: player locale and shared messages.
- `permissions/`: permission metadata and Bukkit registration.
- `updateChecker/`, `other/`: update requests and asynchronous-action feedback.

Settings live in `plugins/yvtils/config.yml`; translations in `languages/`.
`./gradlew :common:build :core:shadowJar` builds the shared runtime and launcher.
