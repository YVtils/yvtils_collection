package yv.tils.discord.logic.whitelist

// TODO: Fix and optimize the code
class WhitelistConfig {
    companion object {
        var whitelistManager: MutableList<String> = mutableListOf()
    }

    fun importer() {
//        val keys = DiscordConfig.saves.keys
//
//        for (key in keys) {
//            val values = DiscordConfig.saves[key] as String
//
//            if (key == "documentation") continue
//
//            val value = values.split(" ")
//
//            whitelistManager.add(key + "," + value[0] + "," + value[1])
//        }
    }

    fun reader(dc: String? = null, mc: String? = null, uuid: String? = null): MutableList<String> {
        val request: MutableList<String> = mutableListOf()

        loop@ for (manager in whitelistManager) {
            val split = manager.split(",")

            for (i in split) {
                if (i == dc || i == mc || i == uuid) {

                    request.add(split[0])
                    request.add(split[1])
                    request.add(split[2])

                    break@loop
                }
            }
        }

        request.add("null")
        request.add("null")
        request.add("null")

//        Debugger().log(
//            "ImportWhitelist",
//            "InPut: $dc, $mc, $uuid | OutPut: $request",
//            "yv.tils.dc.mods.discord.whitelist.ImportWhitelist.reader"
//        )
        return request
    }
}