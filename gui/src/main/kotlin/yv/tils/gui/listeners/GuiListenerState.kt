package yv.tils.gui.listeners

import yv.tils.gui.logic.GuiHolder
import yv.tils.gui.logic.ListContext
import java.util.UUID

object GuiListenerState {
    val pendingChat = mutableMapOf<UUID, Pair<GuiHolder, String>>()
    val pendingList = mutableMapOf<UUID, ListContext>()
    val pendingAdd = mutableMapOf<UUID, ListContext>()
}

