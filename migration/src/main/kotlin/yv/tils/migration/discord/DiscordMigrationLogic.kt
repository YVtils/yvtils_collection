package yv.tils.migration.discord

class DiscordMigrationLogic {
    fun startMigration() {
        ConfigMigrator.migrateIfNeeded()
        SaveMigrator.migrateIfNeeded()
    }
}
