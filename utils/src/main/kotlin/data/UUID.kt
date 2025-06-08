package data

import java.util.UUID

class UUID {
    companion object {
        val uuids = mutableListOf<UUID>()

        /**
         * Generates a random UUID and checks if it is already registered.
         * If it is, it generates a new one until it finds an unregistered UUID.
         * @return A unique UUID.
         */
        fun generateUUID(): UUID {
            val uuid = UUID.randomUUID()

            if (registerUUID(uuid)) {
                return uuid
            }

            return generateUUID()
        }

        /**
         * Registers a UUID if it is not already registered.
         * @param uuid The UUID to register.
         * @return True if the UUID was registered, false if it was already registered.
         */
        fun registerUUID(uuid: UUID): Boolean {
            if (!uuids.contains(uuid)) {
                uuids.add(uuid)
                return true
            }
            return false
        }

        /**
         * Registers a UUID if it is not already registered.
         * @param uuid The UUID to register.
         * @return True if the UUID was registered, false if it was already registered.
         */
        fun registerUUID(uuid: String): Boolean {
            return registerUUID(UUID.fromString(uuid))
        }
    }
}