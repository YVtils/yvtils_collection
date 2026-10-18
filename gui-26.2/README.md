# GUI — Minecraft 26.2

Version-specific InvUI 2.3.x library selected by core on Paper 26.2.
`build.gradle.kts` compiles the canonical Kotlin sources from
[`gui-26.1`](../gui-26.1/README.md); this folder intentionally has no duplicate source tree.

The shared API provides config editors, styling and controls. Keep Paper API and
InvUI versions coordinated with the core GUI registry. Verify with
`./gradlew :gui-26.2:build`; shared changes require all GUI variants to build.
